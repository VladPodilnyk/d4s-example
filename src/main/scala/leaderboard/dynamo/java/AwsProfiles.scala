package leaderboard.dynamo.java

import izumi.functional.bio.{BIO, BlockingIO, F}
import leaderboard.models.common.UserId
import leaderboard.models.{QueryFailure, UserProfile}
import leaderboard.repo.Profiles
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, UpdateItemRequest}

import scala.jdk.CollectionConverters._

final class AwsProfiles[F[+_, +_]: BIO: BlockingIO](client: DynamoDbClient) extends Profiles[F] {
  override def getProfile(userId: UserId): F[QueryFailure, Option[UserProfile]] = {
    val rq = GetItemRequest
      .builder()
      .tableName(DynamoHelper.profilesTable)
      .key(Map("userId" -> AttributeValue.builder().s(userId.value.toString).build()).asJava)
      .build()

    F.syncBlocking(client.getItem(rq)).map {
        rsp =>
          val data = rsp.item().asScala.toMap
          (data.get("userName"), data.get("description")) match {
            case (Some(userName), Some(descr)) => Some(UserProfile(userName.s(), descr.s()))
            case _                             => None
          }
      }
      .leftMap(err => QueryFailure(err.getMessage, err))

  }

  override def setProfile(userId: UserId, profile: UserProfile): F[QueryFailure, Unit] = {
    val rq = UpdateItemRequest
      .builder()
      .tableName(DynamoHelper.profilesTable)
      .key(Map("userId" -> AttributeValue.builder().s(userId.value.toString).build()).asJava)
      .updateExpression(s"SET userName = :name, description = :descr")
      .expressionAttributeValues(
        Map(
          ":name"  -> AttributeValue.builder().s(profile.userName).build(),
          ":descr" -> AttributeValue.builder().s(profile.description).build()
        ).asJava
      )
      .build()

    F.syncBlocking(client.updateItem(rq)).leftMap(err => QueryFailure(err.getMessage, err)).void
  }
}
