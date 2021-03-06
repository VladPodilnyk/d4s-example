package leaderboard

import distage.Activation
import distage.plugins.PluginConfig
import izumi.distage.model.definition.{Axis, DIResource}
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.distage.roles.{RoleAppLauncher, RoleAppMain}
import izumi.fundamentals.platform.cli.model.raw.{RawEntrypointParams, RawRoleParams}
import leaderboard.effects.{ConcurrentThrowable, TTimer}
import leaderboard.http.HttpApi
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._

import scala.concurrent.ExecutionContext.global

final class LeaderboardServiceRole[F[+_, +_]: ConcurrentThrowable: TTimer](httpApi: HttpApi[F]) extends RoleService[F[Throwable, ?]] {
  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): DIResource.DIResourceBase[F[Throwable, ?], Unit] = {
    for {
      _ <- DIResource.fromCats {
        BlazeServerBuilder
          .apply[F[Throwable, ?]](global)
          .withHttpApp(httpApi.routes.orNotFound)
          .bindLocal(8080)
          .resource
      }
    } yield ()
  }
}

object MainProd extends MainBase(Activation(CustomAxis -> CustomAxis.Prod))
object MainDummy extends MainBase(Activation(CustomAxis -> CustomAxis.Dummy))

object LeaderboardServiceRole extends RoleDescriptor {
  val id = "leaderboard"
}

sealed abstract class MainBase(activation: Activation)
  extends RoleAppMain.Default(
    launcher = new RoleAppLauncher.LauncherBIO[zio.IO] {
      override val pluginConfig        = PluginConfig.cached(packagesEnabled = Seq("leaderboard.plugins"))
      override val requiredActivations = activation
    }
  ) {
  override val requiredRoles = Vector(
    RawRoleParams(LeaderboardServiceRole.id)
  )
}

object CustomAxis extends Axis {
  override def name: String = "custom-axis"
  case object Prod extends AxisValueDef
  case object Dummy extends AxisValueDef
}
