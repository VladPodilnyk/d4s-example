package leaderboard

import distage.Activation
import distage.plugins.PluginConfig
import izumi.distage.model.definition.DIResource
import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.distage.roles.{RoleAppLauncher, RoleAppMain}
import izumi.fundamentals.platform.cli.model.raw.{RawEntrypointParams, RawRoleParams}
import leaderboard.effects.{ConcurrentThrowable, TTimer}
import leaderboard.http.HttpApi
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._

final class LeaderboardServiceRole[F[+_, +_]: ConcurrentThrowable: TTimer](
  httpApi: HttpApi[F]
) extends RoleService[F[Throwable, ?]] {
  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): DIResource.DIResourceBase[F[Throwable, ?], Unit] = {
    DIResource.fromCats {
      BlazeServerBuilder[F[Throwable, ?]]
        .withHttpApp(httpApi.routes.orNotFound)
        .bindLocal(8080)
        .resource
    }.void
  }
}

object LeaderboardServiceRole extends RoleDescriptor {
  val id = "Leaderboard"
}

object GenericLauncher extends MainBase(Activation(Repo -> Repo.Prod)) {
  override val requiredRoles = Vector.empty
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
