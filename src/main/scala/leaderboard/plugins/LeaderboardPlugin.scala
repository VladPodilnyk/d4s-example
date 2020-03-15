package leaderboard.plugins

import akka.stream.alpakka.dynamodb.DynamoClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import d4s.models.table.TableDef
import distage.config.ConfigModuleDef
import distage.plugins.PluginDef
import distage.{ModuleDef, TagKK}
import leaderboard.config.{DynamoCfg, ProvisioningCfg}
import leaderboard.dynamo.d4s.{D4SLadder, D4SProfiles, LadderTable, ProfilesTable}
import leaderboard.dynamo.java.{AwsLadder, AwsProfiles, DynamoHelper}
import leaderboard.dynamo.scanamo.{AlpakkaLadder, AlpakkaProfiles, ScanamoLadder, ScanamoUtils}
import leaderboard.http.HttpApi
import leaderboard.repo.{Ladder, Profiles, Ranks}
import leaderboard.{CustomAxis, LeaderboardServiceRole}
import org.http4s.dsl.Http4sDsl
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import zio.IO

object LeaderboardPlugin extends PluginDef {
  include(modules.roles[IO])
  include(modules.api[IO])
  include(modules.repoDummy[IO])
  include(modules.repoAmz[IO])
  include(modules.repoScanamo[IO])
  include(modules.clients)
  include(modules.configs)
  include(d4s.modules.D4SModule[IO])

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
      make[Ladder[F]].from[AwsLadder[F]].named("aws-ladder")
      make[Profiles[F]].from[AwsProfiles[F]].named("aws-profiles")
    }

    def repoScanamo[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(CustomAxis.Scanamo)
      make[Ladder[F]].from[ScanamoLadder[F]].named("scanamo-ladder")
      make[Profiles[F]].from[AwsProfiles[F]].named("scanamo-profiles")
    }

    def repoAlpakkaAndScanamo[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(CustomAxis.Alpakka)
      make[Ladder[F]].from[AlpakkaLadder[F]]
      make[Profiles[F]].from[AlpakkaProfiles[F]]
    }

    val clients: ModuleDef = new ModuleDef {
      make[DynamoDbClient].from {
        cfg: DynamoCfg =>
          DynamoHelper.makeClient(cfg)
      }

      make[AmazonDynamoDB].from {
        cfg: DynamoCfg =>
          ScanamoUtils.makeClient(cfg)
      }

      make[DynamoClient].from {
        cfg: DynamoCfg =>
          ScanamoUtils.makeAlpakkaClient(cfg)
      }
    }

    def repoD4S[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(CustomAxis.D4S)

      make[LadderTable]
      make[ProfilesTable]

      many[TableDef]
        .weak[LadderTable]
        .weak[ProfilesTable]

      make[Ladder[F]].from[D4SLadder[F]].named("d4s-ladder")
      make[Profiles[F]].from[D4SProfiles[F]].named("d4s-profiles")
    }

    val configs: ConfigModuleDef = new ConfigModuleDef {
      makeConfig[DynamoCfg]("aws.dynamo")
      makeConfig[ProvisioningCfg]("aws.dynamo.provisioning")
    }
  }
}
