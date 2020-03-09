package leaderboard.dynamo.scanamo

import akka.stream.alpakka.dynamodb.DynamoClient
import izumi.functional.bio.{BIOAsync, F}
import leaderboard.dynamo.scanamo.ScanamoUtils.profilesTableDef
import leaderboard.models
import leaderboard.models.{QueryFailure, UserId, UserProfile}
import leaderboard.repo.Profiles
import org.scanamo.ScanamoAlpakka
import org.scanamo.syntax._

final class AlpakkaProfiles[F[+_, +_]: BIOAsync](client: DynamoClient) extends Profiles[F] {
  override def getProfile(userId: UserId): F[QueryFailure, Option[UserProfile]] = {
    F.fromFuture(ScanamoAlpakka(client).execFuture(profilesTableDef.get("userId" -> userId.value.toString))).flatMap {
        case None => F.pure(None)
        case Some(value) =>
          value match {
            case Left(err)    => F.fail(new Throwable(err.toString)): F[Throwable, Option[UserProfile]]
            case Right(value) => F.pure(Some(value.profile))
          }
      }.leftMap(err => QueryFailure(err.toString, err))
  }

  override def setProfile(userId: models.UserId, profile: UserProfile): F[QueryFailure, Unit] = {
    F.fromFuture(
        ScanamoAlpakka(client).execFuture(
          profilesTableDef
            .update(
              "userId" -> userId.value.toString,
              set("name", profile.name) and set("description", profile.description)
            )
        )
      ).flatMap {
        case Left(err) => F.fail(new Throwable(err.toString)): F[Throwable, Unit]
        case Right(_)  => F.unit
      }.leftMap(err => QueryFailure(err.toString, err))
  }
}
