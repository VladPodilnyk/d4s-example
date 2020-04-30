package leaderboard.dynamo.d4s

import d4s.DynamoConnector
import izumi.functional.bio.BIO
import leaderboard.dynamo.d4s.LadderTable._
import leaderboard.models.common.{Score, UserId}
import leaderboard.models.{QueryFailure, UserWithScore}
import leaderboard.repo.Ladder

final class D4SLadder[F[+_, +_]: BIO](connector: DynamoConnector[F], ladderTable: LadderTable) extends Ladder[F] {
  import ladderTable._

  override def getScores: F[QueryFailure, List[UserWithScore]] = {
    connector
      .run("get scores query") {
        table.scan.decodeItems[UserIdWithScoreStored].execPagedFlatten()
      }
      .leftMap(err => QueryFailure(err.message, err.cause))
      .map(_.map(_.toAPI))
  }

  override def submitScore(userId: UserId, score: Score): F[QueryFailure, Unit] = {
    connector
      .run("submit user's score") {
        table.updateItem(UserIdWithScoreStored(userId.value, score.value))
      }.leftMap(err => QueryFailure(err.message, err.cause)).void
  }
}
