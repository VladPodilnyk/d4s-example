package leaderboard.dynamo.java

import java.util.UUID

import izumi.functional.bio.{BIO, BlockingIO, F}
import leaderboard.models.{QueryFailure, Score, UserId, UserWithScore}
import leaderboard.repo.Ladder
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, ScanRequest, UpdateItemRequest}

import scala.jdk.CollectionConverters._

final class AwsLadder[F[+_, +_]: BIO: BlockingIO](client: DynamoDbClient) extends Ladder[F] {

  override def getScores: F[QueryFailure, List[UserWithScore]] = {
    val rq = ScanRequest
      .builder()
      .tableName(DynamoHelper.ladderTable)
      .build()

    F.syncBlocking(client.scan(rq).items().asScala.map(_.asScala))
      .leftMap(err => QueryFailure(err.getMessage, err))
      .map(_.iterator.map {
        item =>
          (item.get("userId"), item.get("score")) match {
            case (Some(id), Some(score)) => UserWithScore(UserId(UUID.fromString(id.s())), Score(score.n().toLong))
          }

      }.toList)

  }

  override def submitScore(userId: UserId, score: Score): F[QueryFailure, Unit] = {
    val rq = UpdateItemRequest
      .builder()
      .tableName(DynamoHelper.ladderTable)
      .key(Map("userId" -> AttributeValue.builder().s(userId.value.toString).build()).asJava)
      .updateExpression(s"SET score = :score")
      .expressionAttributeValues(Map(":score" -> AttributeValue.builder().n(score.value.toString).build()).asJava)
      .build()

    F.syncBlocking(client.updateItem(rq)).leftMap(err => QueryFailure(err.getMessage, err)).void
  }

  private[this] def processPages(rq: ScanRequest) = {
    import scala.jdk.CollectionConverters._

    @scala.annotation.tailrec
    def loop(acc: List[Map[String, AttributeValue]], rq: ScanRequest): List[Map[String, AttributeValue]] = {
      val rsp  = client.scan(rq)
      val next = rsp.lastEvaluatedKey()
      if (!next.isEmpty) {
        val items = rsp.items().asScala.toList.map(_.asScala.toMap)
        loop(items ++ acc, rq.toBuilder.exclusiveStartKey(next).build())
      } else
        acc
    }

    loop(List.empty, rq)
  }
}
