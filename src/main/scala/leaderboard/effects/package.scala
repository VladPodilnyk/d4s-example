package leaderboard

import cats.effect.{ConcurrentEffect, Timer}

package object effects {
  type ConcurrentThrowable[F[_, _]] = ConcurrentEffect[F[Throwable, ?]]
  type TTimer[F[_, _]]              = Timer[F[Throwable, ?]]
  object TTimer {
    def apply[F[_, _]: TTimer]: TTimer[F] = implicitly
  }
}
