package leaderboard.models

import java.util.UUID

import io.circe.derivation.deriveCodec
import io.circe.syntax._
import io.circe.{Codec, Decoder, DecodingFailure, Encoder, derivation}
import net.playq.metrics.base.MetricDef
import net.playq.metrics.base.MetricDef._

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
    implicit val metricCounterCodec: Codec.AsObject[MetricCounter] = derivation.deriveCodec[MetricCounter]
    implicit val metricHistogramCodec: Codec.AsObject[MetricHistogram] = derivation.deriveCodec[MetricHistogram]
    implicit val metricTimerCodec: Codec.AsObject[MetricTimer] = derivation.deriveCodec[MetricTimer]
    implicit val metricMeterCodec: Codec.AsObject[MetricMeter] = derivation.deriveCodec[MetricMeter]
    implicit val metricGaugeCodec: Codec.AsObject[MetricGauge] = derivation.deriveCodec[MetricGauge]

    implicit val encodeMetricDef: Encoder.AsObject[MetricDef] = Encoder.AsObject.instance({
      case v: MetricCounter =>
        Map("counter" -> v).asJsonObject
      case v: MetricTimer =>
        Map("timer" -> v).asJsonObject
      case v: MetricMeter =>
        Map("meter" -> v).asJsonObject
      case v: MetricHistogram =>
        Map("histogram" -> v).asJsonObject
      case v: MetricGauge =>
        Map("gauge" -> v).asJsonObject
    })
    implicit val decodeMetricDef: Decoder[MetricDef] = Decoder.instance(c => {
      val maybeContent =
        c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname);
           result <- fname match {
             case "counter" =>
               value.as[MetricCounter]
             case "timer" =>
               value.as[MetricTimer]
             case "meter" =>
               value.as[MetricMeter]
             case "histogram" =>
               value.as[MetricHistogram]
             case "gauge" =>
               value.as[MetricGauge]
             case _ =>
               val cname = "net.playq.metrics.MetricDef"
               val alts = List("counter", "timer", "meter", "histogram", "gauge").mkString(",")
               Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
           }) yield {
        result
      }
    })
  }
}
