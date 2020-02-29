package leaderboard.models

import io.circe.{Codec, derivation}

final case class UserWithScore(id: UserId, score: Score)

object UserWithScore {
  implicit val codec: Codec.AsObject[UserWithScore] = derivation.deriveCodec
}
