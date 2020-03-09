package leaderboard

import cats.effect.{Async, ConcurrentEffect, ContextShift, Timer}

package object effects {
  type ConcurrentThrowable[F[_, _]] = ConcurrentEffect[F[Throwable, ?]]

  type ContextShiftThrowable[F[_, _]] = ContextShift[F[Throwable, ?]]
  object ContextShiftThrowable {
    def apply[F[_, _]: ContextShiftThrowable]: ContextShiftThrowable[F] = implicitly
  }

  type AsyncThrowable[F[_, _]] = Async[F[Throwable, ?]]
  object AsyncThrowable {
    def apply[F[_, _]: AsyncThrowable]: AsyncThrowable[F] = implicitly
  }

  type TTimer[F[_, _]]              = Timer[F[Throwable, ?]]
  object TTimer {
    def apply[F[_, _]: TTimer]: TTimer[F] = implicitly
  }
}
