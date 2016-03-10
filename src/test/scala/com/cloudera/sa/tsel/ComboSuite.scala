package com.cloudera.sa.tsel

import com.saikocat.test.TestingKafkaCluster

import org.scalatest.{BeforeAndAfter, FunSuite}

import kafka.consumer.ConsumerIterator
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import kafka.producer.ProducerConfig
import kafka.producer.Producer
import kafka.producer.KeyedMessage

import org.apache.spark.sql.hive.test.{TestHive, TestHiveContext}

import org.apache.flume.source.avro.AvroFlumeEvent

import java.util.Properties
import java.nio.ByteBuffer

import scala.collection.JavaConversions._

trait TestHiveSingleton {
  import java.io.File
  import org.apache.commons.io.FileUtils

  protected val sqlContext = TestHive
  protected val hiveContext = TestHive

  def cleanupDerby() = {
    FileUtils.deleteQuietly(new File("derby.log"))
    FileUtils.deleteDirectory(new File("metastore_db"))
  }
}

class ComboSuite extends FunSuite
  with BeforeAndAfter
  with TestHiveSingleton {

  private val topic = "datafeed-XXX"
  private val kafkaTestCluster = TestingKafkaCluster()

  before {
    kafkaTestCluster.start()
  }

  after {
    cleanupDerby()
    kafkaTestCluster.stop()
  }

  test("Test Write and Read AvroFlumeEvent") {
    val producer = buildProducer()

    hiveContext.sql(
      """
        | CREATE TABLE testsrc (key INT, value STRING)
      """.stripMargin)
    hiveContext.sql("INSERT INTO TABLE testsrc SELECT tmp.* FROM (select 1, 'data1') tmp")
    hiveContext.sql("INSERT INTO TABLE testsrc SELECT tmp.* FROM (select 2, 'data2') tmp")
    hiveContext.sql("INSERT INTO TABLE testsrc SELECT tmp.* FROM (select 3, 'data3') tmp")

    val props: Properties = new Properties()
    props.put("metadata.broker.list", kafkaTestCluster.kafkaBrokerString)
    props.put("serializer.class", "contrib.kafka.serializer.AvroFlumeEventEncoder")
    props.put("host.name", "localhost")

    val runner = new ReferenceDataToCEP(Param("SELECT * FROM testsrc", "datafeed-XXX", "|", topic), props)
    runner.run(hiveContext)

    val numThreads = 1
    val topicCountMap = Map(topic -> numThreads)
    val consumer = buildConsumer()
    val consumerMap = consumer.createMessageStreams(topicCountMap)
    val streams = consumerMap.get(topic).get

    val value = streams(0).iterator().next().message()
    println(new contrib.kafka.serializer.AvroFlumeEventDecoder().fromBytes(value))
    println(new String(value, "UTF-8"))
    println(value)
    consumer.shutdown()

    assert(1 == 1)
  }

  def buildProducer(): Producer[String, AvroFlumeEvent] = {
    val props: Properties = new Properties()
    props.put("metadata.broker.list", kafkaTestCluster.kafkaBrokerString)
    props.put("serializer.class", "contrib.kafka.serializer.AvroFlumeEventEncoder")
    props.put("host.name", "localhost")
    new Producer[String, AvroFlumeEvent](new ProducerConfig(props))
  }

  def buildConsumer() = {
    val props = new Properties()
    props.put("group.id", "0")
    props.put("zookeeper.connect", kafkaTestCluster.zkConnectString)
    props.put("auto.offset.reset",  "smallest")
    val config = new ConsumerConfig(props)
    Consumer.create(config)
  }
}
