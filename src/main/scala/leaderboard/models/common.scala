package leaderboard.models

import java.util.UUID

import io.circe.derivation.deriveCodec
import io.circe.{Codec, Decoder, Encoder}
import net.playq.metrics.base.MetricDef

object common {
  final case class UserId(value: UUID) extends AnyVal
  object UserId {
    implicit val codec: Codec.AsObject[UserId] = deriveCodec[UserId]
  }
  final case class Score(value: Long) extends AnyVal
  object Score {
    implicit val codec: Codec.AsObject[Score] = deriveCodec[Score]
  }

  /** You need to provide codecs for MetricsDef to pass it over http. */
  object MetricsCodecs {
    implicit val enc: Encoder[MetricDef] = Encoder[String].contramap[MetricDef](MetricDef.encode)
    implicit val dec: Decoder[MetricDef] = Decoder[String].emap(m => MetricDef.decode(m).left.map(_.getMessage))
  }
}
