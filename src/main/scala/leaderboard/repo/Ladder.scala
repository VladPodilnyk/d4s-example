package leaderboard.repo

import izumi.distage.model.definition.DIResource
import izumi.functional.bio.{BIOMonad, BIOPrimitives, F}
import leaderboard.models.{QueryFailure, Score, UserId, UserWithScore}

trait Ladder[F[_, _]] {
  def submitScore(userId: UserId, score: Score): F[QueryFailure, Unit]
  def getScores: F[QueryFailure, List[UserWithScore]]
}

object Ladder {

  final class Dummy[F[+_, +_]: BIOMonad: BIOPrimitives]
    extends DIResource.LiftF[F[Throwable, ?], Ladder[F]](
      for {
        state <- F.mkRef(Map.empty[UserId, Score])
      } yield {
        new Ladder[F] {
          override def submitScore(userId: UserId, score: Score): F[QueryFailure, Unit] = {
            state.update_(_ + (userId -> score))
          }

          override def getScores: F[QueryFailure, List[UserWithScore]] = {
            state.get.map(_.toList.map { case (id, score) => UserWithScore(id, score) })
          }
        }
      }
    )
}
