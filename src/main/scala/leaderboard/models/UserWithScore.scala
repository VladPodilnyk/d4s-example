package leaderboard.models

import io.circe.Codec
import io.circe.derivation.deriveCodec

final case class UserWithScore(id: UserId, score: Score)

object UserWithScore {
  implicit val codec: Codec.AsObject[UserWithScore] = deriveCodec[UserWithScore]
}
