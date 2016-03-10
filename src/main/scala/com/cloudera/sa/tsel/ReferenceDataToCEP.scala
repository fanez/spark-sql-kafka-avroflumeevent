package com.cloudera.sa.tsel

import kafka.producer.KeyedMessage

import org.apache.flume.source.avro.AvroFlumeEvent

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, Row}
import org.apache.spark.sql.hive.HiveContext

import org.cloudera.spark.streaming.kafka.KafkaWriter._

import java.nio.ByteBuffer
import java.util.Properties

import scala.collection.JavaConversions._

class ReferenceDataToCEP(
  private val param: Param,
  val kafkaProducerConfProp: Properties) extends Serializable {

  def run(hiveContext: HiveContext): Unit = {
    val rdd = hiveContext.sql(param.sql).rdd
    writeToKafka(rdd, kafkaProducerConfProp)
  }

  def serializerFunc(data: Row): KeyedMessage[String, AvroFlumeEvent] = {
    val event = new AvroFlumeEvent(
      buildHeaders(),
      ByteBuffer.wrap(data.mkString(param.delimiter).getBytes))
    new KeyedMessage(param.topic, event)
  }

  def buildHeaders(): Map[CharSequence, CharSequence] = {
    Map[CharSequence, CharSequence](
      "datafeed" -> param.datafeed
    )
  }

  def writeToKafka(rdd: RDD[Row], producerConfProp: Properties): Unit = {
    rdd.writeToKafka(producerConfProp, serializerFunc)
  }
}

case class Param(
    sql: String,
    datafeed: String,
    delimiter: String,
    topic: String)
