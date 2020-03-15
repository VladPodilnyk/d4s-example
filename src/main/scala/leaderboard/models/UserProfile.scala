package leaderboard.models

import io.circe.Codec
import io.circe.derivation.deriveCodec

final case class UserProfile(
  userName: String,
  description: String,
)

object UserProfile {
  implicit val codec: Codec.AsObject[UserProfile] = deriveCodec[UserProfile]
}
