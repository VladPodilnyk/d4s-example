package leaderboard.models

import io.circe.Codec
import io.circe.derivation.deriveCodec
import leaderboard.models.common.UserId

final case class UserProfileWithId(userId: UserId, profile: UserProfile)

object UserProfileWithId {
  implicit val codec: Codec.AsObject[UserProfileWithId] = deriveCodec[UserProfileWithId]
}
