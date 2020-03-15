package leaderboard.models

import io.circe.Codec
import io.circe.derivation.deriveCodec

final case class UserProfileWithId(id: UserId, profile: UserProfile)

object UserProfileWithId {
  implicit val codec: Codec.AsObject[UserProfileWithId] = deriveCodec[UserProfileWithId]
}
