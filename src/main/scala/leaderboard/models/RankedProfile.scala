package leaderboard.models

import io.circe.Codec
import io.circe.derivation.deriveCodec

final case class RankedProfile(
  name: String,
  description: String,
  rank: Int,
  score: Score,
)

object RankedProfile {
  implicit val codec: Codec.AsObject[RankedProfile] = deriveCodec[RankedProfile]
}
