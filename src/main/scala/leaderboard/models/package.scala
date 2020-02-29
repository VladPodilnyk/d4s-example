package leaderboard

import java.util.UUID

package object models {
  final case class UserId(value: UUID) extends AnyVal
  final case class Score(value: Long) extends AnyVal
}
