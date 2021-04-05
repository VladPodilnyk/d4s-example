package leaderboard

import d4s.DynamoDDLService
import d4s.test.envs.DynamoTestEnv.DDLDown
import izumi.distage.model.definition.StandardAxis.{Repo, Scene}
import izumi.distage.model.definition.{Activation, ModuleDef}
import izumi.distage.model.reflection.DIKey
import izumi.distage.plugins.PluginConfig
import izumi.distage.testkit.TestConfig
import izumi.distage.testkit.scalatest.{AssertIO3, Spec3}
import leaderboard.Rnd._
import leaderboard.models.UserProfile
import leaderboard.models.common.{Score, UserId}
import leaderboard.repo.{Ladder, Profiles, Ranks}
import net.playq.aws.tagging.AwsNameSpace
import zio.{IO, ZIO}

abstract class LeaderboardTest extends Spec3[ZIO] with AssertIO3[ZIO]  {
  override def config = TestConfig(
    pluginConfig = PluginConfig.cached(packagesEnabled = Seq("leaderboard.plugins")),
    moduleOverrides = new ModuleDef {
      make[Rnd[IO]].from[Rnd.Impl[IO]]
    },
    activation = Activation(Scene -> Scene.Managed),
    memoizationRoots = Set(
      DIKey.get[AwsNameSpace],
      DIKey.get[Ladder[IO]],
      DIKey.get[Profiles[IO]],
      DIKey.get[DynamoDDLService[IO]],
      DIKey.get[DDLDown[IO]],
    ),
    configBaseName = "leaderboard-test",
  )
}

trait DummyTest extends LeaderboardTest {
  override final def config = super.config.copy(
    activation = super.config.activation ++ Activation(Repo -> Repo.Dummy)
  )
}

trait ProdD4STest extends LeaderboardTest {
  override final def config = super.config.copy(
    activation = super.config.activation ++ Activation(Repo -> Repo.Prod)
  )
}

final class LadderTestDummy extends LadderTest with DummyTest
final class ProfilesTestDummy extends ProfilesTest with DummyTest
final class RanksTestDummy extends RanksTest with DummyTest

final class LadderTestD4S extends LadderTest with ProdD4STest
final class ProfilesTestD4S extends ProfilesTest with ProdD4STest
final class RanksTestD4S extends RanksTest with ProdD4STest

abstract class LadderTest extends LeaderboardTest {

  "Ladder" should {
    "submit & get" in {
      (rnd: Rnd[IO], ladder: Ladder[IO]) =>
        for {
          user  <- rnd[UserId]
          score <- rnd[Score]
          _     <- ladder.submitScore(user, score)
          res   <- ladder.getScores.map(_.find(_.userId == user).map(_.score))
          _     <- assertIO(res contains score)
        } yield ()
    }
  }
}

abstract class ProfilesTest extends LeaderboardTest {
  "Profiles" should {
    "set & get" in {
      (rnd: Rnd[IO], profiles: Profiles[IO]) =>
        for {
          user    <- rnd[UserId]
          profile <- rnd[UserProfile]
          _       <- profiles.setProfile(user, profile)
          res     <- profiles.getProfile(user)
          _       <- assertIO(res contains profile)
        } yield ()
    }
  }
}

abstract class RanksTest extends LeaderboardTest {
  "Ranks" should {
    "return None for a user with no score" in {
      (rnd: Rnd[IO], ranks: Ranks[IO], profiles: Profiles[IO]) =>
        for {
          user    <- rnd[UserId]
          profile <- rnd[UserProfile]
          _       <- profiles.setProfile(user, profile)
          res1    <- ranks.getRank(user)
          _       <- assertIO(res1.isEmpty)
        } yield ()
    }

    "return None for a user with no profile" in {
      (rnd: Rnd[IO], ranks: Ranks[IO], ladder: Ladder[IO]) =>
        for {
          user  <- rnd[UserId]
          score <- rnd[Score]
          _     <- ladder.submitScore(user, score)
          res1  <- ranks.getRank(user)
          _     <- assertIO(res1.isEmpty)
        } yield ()
    }

    "assign a higher rank to a user with more score" in {
      (rnd: Rnd[IO], ranks: Ranks[IO], profiles: Profiles[IO], ladder: Ladder[IO]) =>
        for {
          user1    <- rnd[UserId]
          profile1 <- rnd[UserProfile]
          score1   <- rnd[Score]

          user2    <- rnd[UserId]
          profile2 <- rnd[UserProfile]
          score2   <- rnd[Score]

          _ <- profiles.setProfile(user1, profile1)
          _ <- ladder.submitScore(user1, score1)

          _ <- profiles.setProfile(user2, profile2)
          _ <- ladder.submitScore(user2, score2)

          user1Rank <- ranks.getRank(user1).map(_.get.rank)
          user2Rank <- ranks.getRank(user2).map(_.get.rank)

          _ <-
            if (score1.value > score2.value) {
              assertIO(user1Rank > user2Rank)
            } else if (score1.value < score2.value) {
              assertIO(user1Rank < user2Rank)
            } else IO.unit
        } yield ()
    }
  }
}
