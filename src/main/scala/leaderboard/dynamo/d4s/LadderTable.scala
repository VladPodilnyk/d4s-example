package leaderboard.dynamo.d4s

import java.util.UUID

import d4s.config.DynamoMeta
import d4s.models.table.{DynamoField, DynamoKey, TableDDL, TableDef, TableReference}
import leaderboard.models.UserId
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

final class LadderTable(implicit meta: DynamoMeta) extends TableDef {
  private[this] val mainKey = DynamoKey(hashKey = DynamoField[UUID]("userId"))

  override val table: TableReference = TableReference("d4s-ladder-table", mainKey)

  override val ddl: TableDDL = TableDDL(table)

  def mainFullKey(userId: UserId): Map[String, AttributeValue] = {
    mainKey.bind(userId.value)
  }
}
