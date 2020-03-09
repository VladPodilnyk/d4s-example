package leaderboard.dynamo.scanamo

import cats.instances.either._
import cats.instances.list._
import cats.syntax.traverse._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import izumi.functional.bio.{BIO, F}
import leaderboard.dynamo.scanamo.ScanamoUtils._
import leaderboard.models.{QueryFailure, Score, UserId, UserWithScore}
import leaderboard.repo.Ladder
import org.scanamo._

final class ScanamoLadder[F[+_, +_]: BIO](client: AmazonDynamoDB) extends Ladder[F] {

  override def getScores: F[QueryFailure, List[UserWithScore]] = {
    Scanamo(client).exec(ladderTableDef.scan()).sequence match {
      case Left(err)              => F.fail(QueryFailure(err.toString, new Throwable("DynamoReadError")))
      case Right(usersWithScores) => F.pure(usersWithScores)
    }
  }

  override def submitScore(userId: UserId, score: Score): F[QueryFailure, Unit] = {
    F.syncThrowable(Scanamo(client).exec {
      ladderTableDef.put(UserWithScore(userId, score))
    }).leftMap(err => QueryFailure(err.toString, err))
  }
}
