package leaderboard.models

import java.util.UUID

import io.circe.Codec
import io.circe.derivation.deriveCodec

object common {
  final case class UserId(value: UUID) extends AnyVal
  object UserId {
    implicit val codec: Codec.AsObject[UserId] = deriveCodec[UserId]
  }
  final case class Score(value: Long) extends AnyVal
  object Score {
    implicit val codec: Codec.AsObject[Score] = deriveCodec[Score]
  }
}
