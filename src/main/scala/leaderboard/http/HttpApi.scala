package leaderboard.http

import io.circe.syntax._
import izumi.functional.bio.BIO
import izumi.functional.bio.catz._
import leaderboard.models.{Score, UserId, UserProfile}
import leaderboard.repo.{Ladder, Profiles, Ranks}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

trait HttpApi[F[_, _]] {
  def routes: HttpRoutes[F[Throwable, ?]]
}

object HttpApi {

  final class Impl[F[+_, +_]: BIO](
    ladder: Ladder[F],
    profiles: Profiles[F],
    ranks: Ranks[F]
  ) extends HttpApi[F]
    with Http4sDsl[F[Throwable, ?]] {

    private val prefixPath = "/LeaderboardService"

    override def routes: HttpRoutes[F[Throwable, ?]] = Router(
      prefixPath -> httpRoutes
    )

    private val httpRoutes: HttpRoutes[F[Throwable, ?]] = HttpRoutes.of[F[Throwable, ?]] {
      case GET -> Root / "ladder" =>
        Ok(ladder.getScores.map(_.asJson))

      case POST -> Root / "ladder" / UUIDVar(userId) / LongVar(score) =>
        Ok(ladder.submitScore(UserId(userId), Score(score)) *> BIO.pure("OK"))

      case GET -> Root / "profile" / UUIDVar(id) =>
        Ok(ranks.getRank(UserId(id)).map(_.asJson))

      case request @ POST -> Root / "profile" / UUIDVar(userId) =>
        Ok(for {
          profile <- request.decodeJson[UserProfile]
          _       <- profiles.setProfile(UserId(userId), profile)
        } yield ())

    }
  }

}
