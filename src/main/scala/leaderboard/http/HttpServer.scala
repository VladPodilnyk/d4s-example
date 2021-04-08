package leaderboard.http

import izumi.distage.model.definition.{Id, Lifecycle}
import leaderboard.effects.{ConcurrentThrowable, TTimer}
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._

import scala.concurrent.ExecutionContext

final case class HttpServer[F[_, _]](server: Server[F[Throwable, ?]])

object HttpServer {
  final class Impl[F[+_, +_]: ConcurrentThrowable: TTimer](
    httpApi: HttpApi[F],
    executionContext: ExecutionContext @Id("zio.cpu"),
  ) extends Lifecycle.Of[F[Throwable, ?], HttpServer[F]](
      Lifecycle.fromCats {
        BlazeServerBuilder
          .apply[F[Throwable, ?]](executionContext)
          .withHttpApp(httpApi.routes.orNotFound)
          .bindLocal(8080)
          .resource
          .map(HttpServer.apply)
      }
    )
}
