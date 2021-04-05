//package leaderboard.plugins
//
//import java.util.concurrent.ThreadPoolExecutor
//
//import cats.effect.{Async, Blocker, Bracket, ConcurrentEffect, ContextShift}
//import distage.Id
//import distage.plugins.PluginDef
//import izumi.distage.effect.modules.ZIODIEffectModule
//import leaderboard.effects.{AsyncThrowable, ConcurrentThrowable, ContextShiftThrowable, TTimer}
//import logstage.LogBIO
//import zio.interop.catz._
//import zio.interop.catz.implicits._
//import zio.{IO, Runtime, Task}
//
//import scala.concurrent.ExecutionContext
//
//object ZIOPlugin extends PluginDef {
//  include(ZIODIEffectModule)
//
//  addImplicit[Bracket[Task, Throwable]]
//  addImplicit[Async[Task]]
//  addImplicit[ContextShiftThrowable[IO]]
//  addImplicit[AsyncThrowable[IO]]
//  addImplicit[ContextShift[Task]]
//  make[TTimer[IO]].from(TTimer[IO])
//
//  make[ConcurrentThrowable[IO]].from {
//    implicit r: Runtime[Any] =>
//      implicitly[ConcurrentEffect[IO[Throwable, ?]]]
//  }
//
//  make[Blocker].from {
//    pool: ThreadPoolExecutor @Id("zio.io") =>
//      Blocker.liftExecutionContext(ExecutionContext.fromExecutorService(pool))
//  }
//
//  make[LogBIO[IO]].from(LogBIO.fromLogger[IO] _)
//}
