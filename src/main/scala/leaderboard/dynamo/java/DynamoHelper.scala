package leaderboard.dynamo.java

import java.net.URI

import izumi.functional.bio.{BIO, BlockingIO, F}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.http.apache.ApacheSdkHttpService
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeDefinition, BillingMode, CreateTableRequest, CreateTableResponse, KeySchemaElement, KeyType, ProvisionedThroughput, ScalarAttributeType}

import scala.util.chaining._

object DynamoHelper {
  final val ladderTable   = "ladder-dynamo-table"
  final val profilesTable = "profiles-dynamo-table"
  final val ranksTable    = "ranks-dynamo-table"

  def makeClient: DynamoDbClient = {
    DynamoDbClient
      .builder()
      .httpClientBuilder(new ApacheSdkHttpService().createHttpClientBuilder())
      .pipe(_.endpointOverride(URI.create("http://localhost:8042")))
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("x", "x")))
      .region(Region.US_EAST_1)
      .build()
  }

  def createTable[F[+_, +_]: BIO: BlockingIO](client: DynamoDbClient, tableName: String): F[Throwable, CreateTableResponse] = {
    val rq = CreateTableRequest
      .builder()
      .tableName(tableName)
      .billingMode(BillingMode.PROVISIONED)
      .keySchema(
        KeySchemaElement.builder().attributeName("userId").keyType(KeyType.HASH).build()
      )
      .attributeDefinitions(AttributeDefinition.builder().attributeName("userId").attributeType(ScalarAttributeType.S).build())
      .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(10L).writeCapacityUnits(10L).build())
      .build()

    F.syncBlocking {
      client.createTable(rq)
    }
  }

}
