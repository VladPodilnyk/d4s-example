package leaderboard.dynamo.d4s

import d4s.DynamoConnector
import izumi.functional.bio.BIO
import leaderboard.models
import leaderboard.models.{QueryFailure, Score, UserWithScore}
import leaderboard.repo.Ladder

final class D4SLadder[F[+_, +_]: BIO](connector: DynamoConnector[F], ladderTable: LadderTable) extends Ladder[F] {
  import ladderTable._

  override def getScores: F[QueryFailure, List[UserWithScore]] = {
    connector
      .run("get scores query") {
        table.scan.decodeItems[UserWithScore]
      }.leftMap(err => QueryFailure(err.queryName, err.cause))
  }

  override def submitScore(userId: models.UserId, score: models.Score): F[QueryFailure, Unit] = {
    connector
      .run("submit user's score") {
        table.updateItem(UserWithScore(userId, score))
      }.leftMap(err => QueryFailure(err.queryName, err.cause)).void
  }
}
