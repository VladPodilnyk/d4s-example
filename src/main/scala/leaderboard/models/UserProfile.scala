package leaderboard.models

import d4s.codecs.D4SCodec
import d4s.codecs.circe.D4SCirceCodec
import io.circe.Codec
import io.circe.derivation.deriveCodec

final case class UserProfile(
  userName: String,
  description: String,
)

object UserProfile {
  implicit val codec: Codec.AsObject[UserProfile] = deriveCodec[UserProfile]
  implicit val d4sCodec: D4SCodec[UserProfile]    = D4SCirceCodec.derive
}
