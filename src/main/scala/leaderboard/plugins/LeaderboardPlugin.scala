package leaderboard.plugins

import distage.plugins.PluginDef
import distage.{ModuleDef, TagKK}
import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.roles.bundled.BundledRolesModule
import leaderboard.LeaderboardServiceRole
import leaderboard.dynamo.java.{AwsLadder, DynamoHelper}
import leaderboard.http.HttpApi
import leaderboard.repo.{Ladder, Profiles, Ranks}
import org.http4s.dsl.Http4sDsl
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import zio.IO

object LeaderboardPlugin extends PluginDef {
  include(modules.roles[IO])
  include(modules.api[IO])
  include(modules.repoDummy[IO])
  include(modules.repoProd[IO])
  //include(modules.configs)

  object modules {
    def roles[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      // The `leaderboard` app
      make[LeaderboardServiceRole[F]]

      // Bundled roles: `help` & `configwriter`
      include(BundledRolesModule[F[Throwable, ?]](version = "1.0.0-SNAPSHOT"))
    }

    def api[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      make[HttpApi[F]].from[HttpApi.Impl[F]]
      make[Ranks[F]].from[Ranks.Impl[F]]

      make[Http4sDsl[F[Throwable, ?]]]
    }

    def repoDummy[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(Repo.Dummy)

      make[Ladder[F]].fromResource[Ladder.Dummy[F]]
      make[Profiles[F]].fromResource[Profiles.Dummy[F]]
    }

    def repoProd[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(Repo.Prod)
      make[DynamoDbClient].from(DynamoHelper.makeClient)
      make[Ladder[F]].from[AwsLadder[F]]
      //make[Profiles[F]].fromResource[Profiles.Postgres[F]]
    }

//    val configs: ConfigModuleDef = new ConfigModuleDef {
//      makeConfig[PostgresCfg]("postgres")
//      makeConfig[PostgresPortCfg]("postgres")
//    }
  }
}
