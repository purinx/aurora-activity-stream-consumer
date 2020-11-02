package com.higherkingpud.activity.stream.consumer

// 絶対に消さないこと
import pureconfig.generic.auto._

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl._
import com.amazonaws.encryptionsdk.AwsCrypto
import com.contxt.kinesis.{ConsumerConfig, KinesisSource}
import com.higherkingpud.activity.stream.consumer.json.MonitoringRecordJson
import io.circe.generic.auto._
import io.circe.parser._
import pureconfig.ConfigSource
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, AwsCredentialsProvider, StaticCredentialsProvider}
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient

object Main extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "esupdater")
  implicit val crypto: AwsCrypto            = new AwsCrypto()
  val conf: ASCConfig                       = ConfigSource.default.loadOrThrow[ASCConfig]

  val credentials: AwsBasicCredentials            = AwsBasicCredentials.create(conf.aws.accessKey, conf.aws.secretKey)
  val credentialsProvider: AwsCredentialsProvider = StaticCredentialsProvider.create(credentials)

  implicit val kinesisClient: KinesisAsyncClient = KinesisAsyncClient
    .builder()
    .credentialsProvider(credentialsProvider)
    .build()

  implicit val dynamoClient: DynamoDbAsyncClient = DynamoDbAsyncClient
    .builder()
    .credentialsProvider(credentialsProvider)
    .build()

  implicit val cloudWatchClient: CloudWatchAsyncClient = CloudWatchAsyncClient
    .builder()
    .credentialsProvider(credentialsProvider)
    .build()

  val consumerConfig: ConsumerConfig = ConsumerConfig.withNames(conf.streamName, conf.appName)

  val source = KinesisSource(consumerConfig)
    .map { record =>
      for {
        json    <- parse(record.data.utf8String)
        mRecord <- json.as[MonitoringRecordJson]
        event   <- ActivityDecoder.decodeActivityEvents(mRecord, conf.dbcResourceId)
      } yield {
        record.markProcessed()
        event
      }
    }
    .runWith(
      Sink.foreach(
        _.fold(
          e => system.log.error(e.getMessage),
          event => system.log.info(event.toString)
        )
      )
    )
}
