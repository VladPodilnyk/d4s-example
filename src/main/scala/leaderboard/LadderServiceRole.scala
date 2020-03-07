package leaderboard

import distage.Activation
import distage.plugins.PluginConfig
import izumi.distage.model.definition.DIResource
import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.distage.roles.{RoleAppLauncher, RoleAppMain}
import izumi.functional.bio.{BIO, BlockingIO}
import izumi.fundamentals.platform.cli.model.raw.{RawEntrypointParams, RawRoleParams}
import leaderboard.dynamo.java.DynamoHelper
import leaderboard.effects.{ConcurrentThrowable, TTimer}
import leaderboard.http.HttpApi
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

final class LeaderboardServiceRole[F[+_, +_]: ConcurrentThrowable: TTimer: BIO: BlockingIO](
  dynamoClient: DynamoDbClient,
  httpApi: HttpApi[F]
) extends RoleService[F[Throwable, ?]] {
  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): DIResource.DIResourceBase[F[Throwable, ?], Unit] = {
    //val dynamoClient = DynamoHelper.makeClient

    for {
      //_ <- DIResource.liftF(DynamoHelper.createTable(dynamoClient, DynamoHelper.ladderTable))
      //_ <- DIResource.liftF(DynamoHelper.createTable(dynamoClient, DynamoHelper.profilesTable))
      _ <- DIResource.fromCats {
        BlazeServerBuilder[F[Throwable, ?]]
          .withHttpApp(httpApi.routes.orNotFound)
          .bindLocal(8080)
          .resource
      }
    } yield ()
  }
}

object MainProd extends MainBase(Activation(Repo -> Repo.Prod))

object MainDummy extends MainBase(Activation(Repo -> Repo.Dummy))

object LeaderboardServiceRole extends RoleDescriptor {
  val id = "leaderboard"
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
