package leaderboard.repo

import izumi.distage.model.definition.DIResource
import izumi.functional.bio.{BIOMonad, BIOPrimitives, F}
import leaderboard.models.{QueryFailure, UserId, UserProfile}

trait Profiles[F[_, _]] {
  def setProfile(userId: UserId, profile: UserProfile): F[QueryFailure, Unit]
  def getProfile(userId: UserId): F[QueryFailure, Option[UserProfile]]
}

object Profiles {
  final class Dummy[F[+_, +_]: BIOMonad: BIOPrimitives]
    extends DIResource.LiftF[F[Throwable, ?], Profiles[F]](
      for {
        state <- F.mkRef(Map.empty[UserId, UserProfile])
      } yield {
        new Profiles[F] {
          override def setProfile(userId: UserId, profile: UserProfile): F[QueryFailure, Unit] = {
            state.update_(_ + (userId -> profile))
          }

          override def getProfile(userId: UserId): F[QueryFailure, Option[UserProfile]] = {
            state.get.map(_.get(userId))
          }
        }
      }
    )
}
