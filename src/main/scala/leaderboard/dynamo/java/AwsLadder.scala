package leaderboard.dynamo.java

import java.util.UUID

import izumi.functional.bio.{BIO, BlockingIO, F}
import leaderboard.models.common.{Score, UserId}
import leaderboard.models.{QueryFailure, UserWithScore}
import leaderboard.repo.Ladder
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, ScanRequest, UpdateItemRequest}

import scala.jdk.CollectionConverters._

final class AwsLadder[F[+_, +_]: BIO: BlockingIO](client: DynamoDbClient) extends Ladder[F] {

  override def getScores: F[QueryFailure, List[UserWithScore]] = {
    F.syncBlocking(processPages)
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

  private[this] def processPages: List[Map[String, AttributeValue]] = {
    import scala.jdk.CollectionConverters._

    @scala.annotation.tailrec
    def loop(acc: List[Map[String, AttributeValue]], rq: ScanRequest): List[Map[String, AttributeValue]] = {
      val rsp   = client.scan(rq)
      val next  = rsp.lastEvaluatedKey()
      val items = rsp.items().asScala.toList.map(_.asScala.toMap)
      if (!next.isEmpty) {
        loop(items ++ acc, rq.toBuilder.exclusiveStartKey(next).build())
      } else
        items ++ acc
    }

    val rq = ScanRequest
      .builder()
      .tableName(DynamoHelper.ladderTable)
      .build()

    loop(List.empty, rq)
  }
}
