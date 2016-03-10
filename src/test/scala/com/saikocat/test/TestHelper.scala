package com.saikocat.test

import java.io.IOException
import java.util.Properties

import kafka.server.KafkaConfig
import kafka.server.KafkaServerStartable
import kafka.utils.TestUtils

import org.apache.curator.test.TestingServer

class TestingKafkaCluster(val kafkaServer: KafkaServerStartable,
                          val zkServer: TestingServer) {

  def start(): Unit = {
    kafkaServer.startup()
  }

  def kafkaConfig(): KafkaConfig = {
    kafkaServer.serverConfig
  }

  def kafkaBrokerString(): String = {
    s"localhost:${kafkaServer.serverConfig.port}"
  }

  def zkConnectString(): String = {
    return zkServer.getConnectString()
  }

  def kafkaPort(): Int = {
    return kafkaServer.serverConfig.port
  }

  @throws(classOf[IOException])
  def stop(): Unit = {
    kafkaServer.shutdown()
    zkServer.stop()
  }
}

object TestingKafkaCluster {
  @throws(classOf[Exception])
  def apply(): TestingKafkaCluster = {
    val zkServer = new TestingServer()
    val config: KafkaConfig = getKafkaConfig(zkServer.getConnectString())
    val kafkaServer = new KafkaServerStartable(config)
    new TestingKafkaCluster(kafkaServer, zkServer)
  }

  def getKafkaConfig(zkConnectString: String): KafkaConfig = {
    val propsI: scala.collection.Iterator[Properties] =
      TestUtils.createBrokerConfigs(1).iterator
    assert(propsI.hasNext)
    val props: Properties = propsI.next()
    assert(props.containsKey("zookeeper.connect"))
    props.put("zookeeper.connect", zkConnectString)
    props.put("host.name", "localhost")
    new KafkaConfig(props)
  }
}
