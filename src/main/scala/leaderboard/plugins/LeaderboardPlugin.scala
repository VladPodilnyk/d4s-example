package leaderboard.plugins

import d4s.models.table.TableDef
import d4s.modules.D4SModule
import distage.plugins.PluginDef
import distage.{Repo, Scene, TagKK}
import izumi.distage.model.definition.ModuleDef
import leaderboard.LeaderboardServiceRole
import leaderboard.dynamo.{D4SLadder, D4SProfiles, LadderTable, ProfilesTable}
import leaderboard.http.{HttpApi, HttpServer}
import leaderboard.repo.{Ladder, Profiles, Ranks}
import net.playq.aws.tagging.modules.AwsTagsModule
import net.playq.metrics.modules.DummyMetricsModule
import org.http4s.dsl.Http4sDsl
import zio.IO

object LeaderboardPlugin extends PluginDef {
  include(modules.roles[IO])
  include(modules.api[IO])
  include(modules.repoDummy[IO])
  include(modules.repoD4S[IO])

  // d4s modules
  include(DummyMetricsModule[IO])
  include(modules.dynamoProvided)

  object modules {
    def roles[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      make[LeaderboardServiceRole[F]]
    }

    def api[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      make[HttpApi[F]].from[HttpApi.Impl[F]]
      make[Ranks[F]].from[Ranks.Impl[F]]

      make[Http4sDsl[F[Throwable, ?]]]

      make[HttpServer[F]].fromResource[HttpServer.Impl[F]]
    }

    def repoDummy[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(Repo.Dummy)

      make[Ladder[F]].fromResource[Ladder.Dummy[F]]
      make[Profiles[F]].fromResource[Profiles.Dummy[F]]
    }

    def repoD4S[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(Repo.Prod)

      make[LadderTable]
      make[ProfilesTable]

      many[TableDef]
        .weak[LadderTable]
        .weak[ProfilesTable]

      make[Ladder[F]].from[D4SLadder[F]]
      make[Profiles[F]].from[D4SProfiles[F]]
    }

    def dynamoProvided: ModuleDef = new ModuleDef {
      tag(Scene.Provided)
      include(AwsTagsModule)
      include(D4SModule[IO])
    }
  }
}
