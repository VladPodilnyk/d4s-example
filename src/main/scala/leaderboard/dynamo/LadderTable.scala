package leaderboard.dynamo

import java.util.UUID

import d4s.codecs.D4SCodec
import d4s.config.DynamoMeta
import d4s.models.table._
import leaderboard.models.UserWithScore
import leaderboard.models.common.{Score, UserId}
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

final class LadderTable(implicit meta: DynamoMeta) extends TableDef {
  private[this] val mainKey = DynamoKey(hashKey = DynamoField[UUID]("userId"))

  override val table: TableReference = TableReference("d4s-ladder-table", mainKey)

  override val ddl: TableDDL = TableDDL(table)

  def mainFullKey(userId: UserId): Map[String, AttributeValue] = {
    mainKey.bind(userId.value)
  }
}

object LadderTable {
  final case class UserIdWithScoreStored(userId: UUID, score: Long){
    def toAPI: UserWithScore = UserWithScore(UserId(userId), Score(score))
  }
  object UserIdWithScoreStored {
    implicit val codec: D4SCodec[UserIdWithScoreStored] = D4SCodec.derived[UserIdWithScoreStored]
  }
}