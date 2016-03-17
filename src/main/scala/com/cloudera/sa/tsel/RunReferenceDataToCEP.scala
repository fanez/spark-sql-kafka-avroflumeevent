package com.cloudera.sa.tsel

import com.beust.jcommander.{JCommander, Parameter, DynamicParameter}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext

import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.hadoop.conf.Configuration

import java.util.Properties
import java.util.{Map => JMap}

import scala.collection.mutable
import scala.collection.JavaConversions._

object RunReferenceDataToCEP {

  object CliArgs {
    @Parameter(names=Array("--topic"), required=true,
               description="Kafka Topic to write to")
    var topic: String = _

    @Parameter(names=Array("--delimiter"), required=true,
               description="Delimiter for event body")
    var delimiter: String = _

    @Parameter(names=Array("--datafeed"), required=true,
               description="Datafeed")
    var datafeed: String = _ 

    @Parameter(names=Array("--sql"), required=true,
               description="SQL string to be executed. Must NOT contain ';'")
    var sql: String = _

    @Parameter(names=Array("--kafka-broker-list"), required=true,
               description="Kafka Metadata Broker List. Comma separated without space")
    var kafkaBrokerList: String = _

    @DynamicParameter(names=Array("-D"), required=false,
               description="Dynamic Properties")
    var dynamicParams: JMap[String, String] = mutable.Map[String, String]()

    @Parameter(names=Array("--debug-config"), required=false,
               description="Print out configuration")
    var debugConfig: Boolean = false
  }

  def main(args: Array[String]): Unit = {

    new JCommander(CliArgs, args.toArray: _*)

    val param = Param(
      CliArgs.sql, CliArgs.datafeed, CliArgs.delimiter, CliArgs.topic)

    val kafkaProducerConfProp = buildKafkaProducerConf(CliArgs.dynamicParams)
    val runner = new ReferenceDataToCEP(param, kafkaProducerConfProp)

    if (CliArgs.debugConfig) {
      println(s"* Params: ${param}")
      println(s"* Kafka Conf: ${kafkaProducerConfProp}")
    }

    val conf = new SparkConf()
    // conf.set("spark.kryo.registrator", classOf[TelkomselKryoRegistrator].getName)
    val sc = new SparkContext(conf)
    val hiveContext = new HiveContext(sc)
    runner.run(hiveContext)
  }

  def buildKafkaProducerConf(dynamicParams: JMap[String, String]): Properties = {
    val kafkaPropPrefix = "kafka."
    val producerConfProp = new Properties()
    producerConfProp.put("metadata.broker.list",
                         CliArgs.kafkaBrokerList)
    producerConfProp.put("serializer.class",
                         "contrib.kafka.serializer.AvroFlumeEventEncoder")
    // Kafka v 0.9
    producerConfProp.put("bootstrap.servers",
                         CliArgs.kafkaBrokerList)
    producerConfProp.put("value.serializer",
                         "contrib.kafka.serializer.AvroFlumeEventEncoder")

    dynamicParams
      .filter(x => x._1.startsWith(kafkaPropPrefix))
      .foldLeft(producerConfProp) { (m: Properties, x: (String, String)) =>
        m.put(x._1.substring(kafkaPropPrefix.length), x._2)
        m
      }

    producerConfProp
  }
}
