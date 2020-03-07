package leaderboard.plugins

import java.util.concurrent.ThreadPoolExecutor

import cats.effect.{Async, Blocker, Bracket, ConcurrentEffect, ContextShift, Timer}
import distage.Id
import distage.plugins.PluginDef
import izumi.distage.effect.modules.ZIODIEffectModule
import leaderboard.effects.{ConcurrentThrowable, TTimer}
import logstage.LogBIO
import zio.{IO, Runtime, Task}
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.ExecutionContext

object ZIOPlugin extends PluginDef {
  include(ZIODIEffectModule)

  addImplicit[Bracket[Task, Throwable]]
  addImplicit[Async[Task]]
  addImplicit[ContextShift[Task]]
  make[TTimer[IO]].from {
    implicit zclock: zio.clock.Clock =>
      TTimer[IO]
  }
  make[ConcurrentThrowable[IO]].from {
    implicit r: Runtime[Any] =>
      implicitly[ConcurrentEffect[IO[Throwable, ?]]]
  }

  make[Blocker].from {
    pool: ThreadPoolExecutor @Id("zio.io") =>
      Blocker.liftExecutionContext(ExecutionContext.fromExecutorService(pool))
  }

  make[LogBIO[IO]].from(LogBIO.fromLogger[IO] _)
}
