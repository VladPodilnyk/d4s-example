package leaderboard.models

import io.circe.Codec
import io.circe.derivation.deriveCodec
import leaderboard.models.common.{Score, UserId}

final case class UserWithScore(userId: UserId, score: Score)

object UserWithScore {
  implicit val codec: Codec.AsObject[UserWithScore] = deriveCodec[UserWithScore]
}
