package leaderboard.dynamo.scanamo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.dynamodb.{DynamoClient, DynamoSettings}
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBAsync, AmazonDynamoDBAsyncClient}
import izumi.distage.model.definition.DIResource
import izumi.functional.bio.{BIO, F}
import leaderboard.config.DynamoCfg
import leaderboard.models.{UserProfileWithId, UserWithScore}
import org.scanamo.Table
import org.scanamo.generic.auto._

import scala.jdk.CollectionConverters._

object ScanamoUtils {
  private[scanamo] val ladderTable   = "scanamo-ladder-table"
  private[scanamo] val profilesTable = "scanamo-profiles-table"

  private[scanamo] val ladderTableDef   = Table[UserWithScore](ladderTable)
  private[scanamo] val profilesTableDef = Table[UserProfileWithId](profilesTable)

  implicit val system       = ActorSystem("scanamo-alpakka")
  implicit val materializer = ActorMaterializer.create(system)
  implicit val executor     = system.dispatcher

  def makeAlpakkaClient(cfg: DynamoCfg): DynamoClient = {
    DynamoClient(
      DynamoSettings(
        region = cfg.region,
        host   = cfg.uri.split(':').head.stripPrefix("http://"),
      ).withPort(cfg.uri.split(':').last.toInt)
        .withCredentialsProvider(new AWSStaticCredentialsProvider(new BasicAWSCredentials("x", "x")))
    )
  }

  def makeClient(cfg: DynamoCfg): AmazonDynamoDBAsync = {
    AmazonDynamoDBAsyncClient
      .asyncBuilder()
      .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("x", "x")))
      .withEndpointConfiguration(new EndpointConfiguration(cfg.uri, cfg.region))
      .withClientConfiguration(new ClientConfiguration().withClientExecutionTimeout(50000).withRequestTimeout(5000))
      .build()
  }

  def tableSetUp[F[+_, +_]: BIO](client: AmazonDynamoDB, cfg: DynamoCfg) = {
    def createTable(tableName: String) = {
      client.createTable(
        List(new AttributeDefinition("userId", ScalarAttributeType.S)).asJava,
        tableName,
        List(new KeySchemaElement("userId", KeyType.HASH)).asJava,
        new ProvisionedThroughput(cfg.provisioning.read, cfg.provisioning.write)
      )
    }

    DIResource.liftF((for {
      _ <- F.syncThrowable(createTable(ladderTable))
      _ <- F.syncThrowable(createTable(profilesTable))
    } yield ()).catchSome { case _: ResourceInUseException => F.unit })
  }

}
