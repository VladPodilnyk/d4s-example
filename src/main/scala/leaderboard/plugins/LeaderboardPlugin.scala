package leaderboard.plugins

import distage.config.ConfigModuleDef
import distage.plugins.PluginDef
import distage.{ModuleDef, TagKK}
import izumi.distage.model.definition.StandardAxis.Repo
import leaderboard.LeaderboardServiceRole
import leaderboard.config.{DynamoCfg, ProvisioningCfg}
import leaderboard.dynamo.java.{AwsLadder, AwsProfiles, DynamoHelper}
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
  include(modules.configs)

  object modules {
    def roles[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      make[LeaderboardServiceRole[F]]
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

    def repoProd[F[+_, +_]: TagKK]: ConfigModuleDef = new ConfigModuleDef {
      tag(Repo.Prod)

      make[DynamoDbClient].from {
        cfg: DynamoCfg =>
          DynamoHelper.makeClient(cfg)
      }
      make[Ladder[F]].from[AwsLadder[F]]
      make[Profiles[F]].from[AwsProfiles[F]]
    }

    val configs: ConfigModuleDef = new ConfigModuleDef {
      makeConfig[DynamoCfg]("aws.dynamo")
      makeConfig[ProvisioningCfg]("aws.dynamo.provisioning")
    }
  }
}
