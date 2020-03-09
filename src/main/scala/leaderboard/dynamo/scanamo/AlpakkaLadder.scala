package leaderboard.dynamo.scanamo

import akka.stream.alpakka.dynamodb.DynamoClient
import cats.instances.either._
import cats.instances.list._
import cats.syntax.traverse._
import izumi.functional.bio.{BIOAsync, F}
import leaderboard.dynamo.scanamo.ScanamoUtils._
import leaderboard.models.{QueryFailure, Score, UserId, UserWithScore}
import leaderboard.repo.Ladder
import org.scanamo._

final class AlpakkaLadder[F[+_, +_]: BIOAsync](client: DynamoClient) extends Ladder[F] {
  override def getScores: F[QueryFailure, List[UserWithScore]] = {
    F.fromFuture(ScanamoAlpakka(client).execFuture(ladderTableDef.scan()))
      .flatMap {
        _.sequence match {
          case Left(err)             => F.fail(new Throwable(err.toString)): F[Throwable, List[UserWithScore]]
          case Right(usersWithScore) => F.pure(usersWithScore)
        }
      }.leftMap(err => QueryFailure(err.toString, err))
  }

  override def submitScore(userId: UserId, score: Score): F[QueryFailure, Unit] = {
    F.fromFuture(ScanamoAlpakka(client).execFuture {
      ladderTableDef.put(UserWithScore(userId, score))
    }).leftMap(err => QueryFailure(err.toString, err))
  }

}
