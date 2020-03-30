package leaderboard.plugins

import d4s.models.table.TableDef
import d4s.modules.D4SModule
import distage.TagKK
import distage.config.ConfigModuleDef
import distage.plugins.PluginDef
import izumi.distage.model.definition.ModuleDef
import leaderboard.config.{DynamoCfg, ProvisioningCfg}
import leaderboard.dynamo.d4s.{D4SLadder, D4SProfiles, LadderTable, ProfilesTable}
import leaderboard.dynamo.java.{AwsLadder, AwsProfiles, DynamoHelper}
import leaderboard.http.HttpApi
import leaderboard.repo.{Ladder, Profiles, Ranks}
import leaderboard.{CustomAxis, LeaderboardServiceRole}
import net.playq.aws.tagging.modules.AwsTagsModule
import net.playq.metrics.modules.DummyMetricsModule
import org.http4s.dsl.Http4sDsl
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import zio.IO

object LeaderboardPlugin extends PluginDef {
  include(modules.roles[IO])
  include(modules.api[IO])
  include(modules.repoDummy[IO])
  include(modules.repoAmz[IO])
  include(DummyMetricsModule[IO])
  include(AwsTagsModule)
  include(D4SModule[IO])
  include(modules.repoD4S[IO])
  include(modules.clients)
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
      tag(CustomAxis.Dummy)

      make[Ladder[F]].fromResource[Ladder.Dummy[F]]
      make[Profiles[F]].fromResource[Profiles.Dummy[F]]
    }

    def repoAmz[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(CustomAxis.Amz)
      make[Ladder[F]].from[AwsLadder[F]]
      make[Profiles[F]].from[AwsProfiles[F]]
    }

    def repoD4S[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(CustomAxis.D4S)

      make[LadderTable]
      make[ProfilesTable]

      many[TableDef]
        .weak[LadderTable]
        .weak[ProfilesTable]

      make[Ladder[F]].from[D4SLadder[F]]
      make[Profiles[F]].from[D4SProfiles[F]]
    }

    val clients: ModuleDef = new ModuleDef {
      make[DynamoDbClient].from {
        cfg: DynamoCfg =>
          DynamoHelper.makeClient(cfg)
      }
    }

    val configs: ConfigModuleDef = new ConfigModuleDef {
      makeConfig[DynamoCfg]("aws.dynamo.amz")
      makeConfig[ProvisioningCfg]("aws.dynamo.amz.provisioning")
    }
  }
}
