package leaderboard

import distage.plugins.PluginConfig
import distage.{Activation, Lifecycle, Mode, Repo, Scene}
import izumi.distage.model.definition
import izumi.distage.model.definition.ModuleDef
import izumi.distage.roles.RoleAppMain
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.functional.bio.Applicative2
import izumi.fundamentals.platform.cli.model.raw.{RawEntrypointParams, RawRoleParams}
import leaderboard.http.HttpServer
import logstage.LogIO2
import zio.IO

import scala.annotation.unused

final class LeaderboardServiceRole[F[+_, +_]: Applicative2](
  @unused runningServer: HttpServer[F],
  log: LogIO2[F],
) extends RoleService[F[Throwable, ?]] {
  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): Lifecycle[F[Throwable, ?], Unit] = {
    Lifecycle.liftF(log.info("Leaderboard service started!"))
  }
}

object LeaderboardServiceRole extends RoleDescriptor {
  val id = "leaderboard"
}

object MainProd extends MainBase(Activation(Repo -> Repo.Prod), Vector(RawRoleParams(LeaderboardServiceRole.id)))
object MainDummy extends MainBase(Activation(Repo -> Repo.Dummy), Vector(RawRoleParams(LeaderboardServiceRole.id)))

sealed abstract class MainBase(activation: Activation, requiredRoles: Vector[RawRoleParams]) extends RoleAppMain.LauncherBIO2[IO] {
  override def requiredRoles(argv: RoleAppMain.ArgV): Vector[RawRoleParams] = requiredRoles

  override val pluginConfig: PluginConfig = PluginConfig.cached(packagesEnabled = Seq("leaderboard.plugins"))

  protected override def roleAppBootOverrides(argv: RoleAppMain.ArgV): definition.Module = super.roleAppBootOverrides(argv) ++ new ModuleDef {
    make[Activation].named("default").fromValue(defaultActivation ++ activation)
  }

  private[this] def defaultActivation = Activation(Scene -> Scene.Provided, Mode -> Mode.Prod)
}
