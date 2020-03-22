package leaderboard.dynamo.java

import java.net.URI

import izumi.distage.model.definition.DIResource
import izumi.functional.bio.{BIO, BlockingIO, F}
import leaderboard.config.DynamoCfg
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.http.apache.ApacheSdkHttpService
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._

import scala.util.chaining._

object DynamoHelper {
  final val ladderTable   = "ladder-dynamo-table"
  final val profilesTable = "profiles-dynamo-table"

  def makeClient(cfg: DynamoCfg): DynamoDbClient = {
    DynamoDbClient
      .builder()
      .httpClientBuilder(new ApacheSdkHttpService().createHttpClientBuilder())
      .pipe(_.endpointOverride(URI.create(cfg.endpointUrl)))
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("x", "x")))
      .region(Region.of(cfg.region))
      .build()
  }

  def tableSetUp[F[+_, +_]: BIO: BlockingIO](client: DynamoDbClient, cfg: DynamoCfg) = {
    DIResource.liftF((for {
      _ <- createTable(client, cfg, ladderTable)
      _ <- createTable(client, cfg, profilesTable)
    } yield ()).catchSome { case _: ResourceInUseException => F.unit })
  }

  private[java] def createTable[F[+_, +_]: BIO: BlockingIO](client: DynamoDbClient, cfg: DynamoCfg, tableName: String): F[Throwable, CreateTableResponse] = {
    val rq = CreateTableRequest
      .builder()
      .tableName(tableName)
      .billingMode(cfg.provisioning.mode)
      .keySchema(
        KeySchemaElement.builder().attributeName("userId").keyType(KeyType.HASH).build()
      )
      .attributeDefinitions(AttributeDefinition.builder().attributeName("userId").attributeType(ScalarAttributeType.S).build())
      .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(cfg.provisioning.read).writeCapacityUnits(cfg.provisioning.write).build())
      .build()

    F.syncBlocking {
      client.createTable(rq)
    }
  }

}
