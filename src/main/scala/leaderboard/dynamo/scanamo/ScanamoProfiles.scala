package leaderboard.dynamo.scanamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import izumi.functional.bio.{BIO, F}
import leaderboard.dynamo.scanamo.ScanamoUtils._
import leaderboard.models.{QueryFailure, UserId, UserProfile}
import leaderboard.repo.Profiles
import org.scanamo._
import org.scanamo.syntax._

final class ScanamoProfiles[F[+_, +_]: BIO](client: AmazonDynamoDB) extends Profiles[F] {
  override def getProfile(userId: UserId): F[QueryFailure, Option[UserProfile]] = {
    Scanamo(client).exec(profilesTableDef.get("userId" -> userId.value.toString)) match {
      case None => F.pure(None)
      case Some(value) =>
        value match {
          case Left(err)    => F.fail(QueryFailure(err.toString, new Throwable(err.toString)))
          case Right(value) => F.pure(Some(value.profile))
        }
    }
  }

  override def setProfile(userId: UserId, profile: UserProfile): F[QueryFailure, Unit] = {
    Scanamo(client).exec(
      profilesTableDef
        .update(
          "userId" -> userId.value.toString,
          set("name", profile.name) and set("description", profile.description)
        )
    ) match {
      case Left(err) => F.fail(QueryFailure(err.toString, new Throwable(err.toString)))
      case Right(_)  => F.unit
    }
  }
}
