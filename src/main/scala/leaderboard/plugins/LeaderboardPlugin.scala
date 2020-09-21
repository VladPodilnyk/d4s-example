package leaderboard.plugins

import d4s.models.table.TableDef
import d4s.modules.D4SModule
import distage.TagKK
import distage.plugins.PluginDef
import izumi.distage.model.definition.ModuleDef
import leaderboard.dynamo.{D4SLadder, D4SProfiles, LadderTable, ProfilesTable}
import leaderboard.http.HttpApi
import leaderboard.repo.{Ladder, Profiles, Ranks}
import leaderboard.{CustomAxis, LeaderboardServiceRole}
import net.playq.aws.tagging.modules.AwsTagsModule
import net.playq.metrics.modules.DummyMetricsModule
import org.http4s.dsl.Http4sDsl
import zio.IO

object LeaderboardPlugin extends PluginDef {
  include(modules.roles[IO])
  include(modules.api[IO])
  include(modules.repoDummy[IO])
  include(DummyMetricsModule[IO])
  include(AwsTagsModule)
  include(D4SModule[IO])
  include(modules.repoD4S[IO])

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

    def repoD4S[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(CustomAxis.Prod)

      make[LadderTable]
      make[ProfilesTable]

      many[TableDef]
        .weak[LadderTable]
        .weak[ProfilesTable]

      make[Ladder[F]].from[D4SLadder[F]]
      make[Profiles[F]].from[D4SProfiles[F]]
    }
  }
}
