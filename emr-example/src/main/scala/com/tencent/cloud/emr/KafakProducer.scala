package com.tencent.cloud.emr

import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.util.Random


/**
  * bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 10 --topic test_topic
  * Created by liubangchen on 2017/11/17.
  */


object KafakProducer {

  def getProps(brokers: String): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("client.id", "aProducerExample")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    return props
  }

  def main(args: Array[String]): Unit = {
    val events = args(0).toInt
    val topic = args(1)
    val brokers = args(2)
    val rnd = new Random()
    val props = getProps(brokers)

    val producer = new KafkaProducer[String, String](props)
    val t = System.currentTimeMillis()
    for (nEvents <- Range(0, events)) {
      val runtime = new Date().getTime()
      val ip = "192.168.2." + rnd.nextInt(255)
      val msg = runtime + "," + nEvents + ",www.example.com," + ip
      val data = new ProducerRecord[String, String](topic, ip, msg)

      //async
      //producer.send(data, (m,e) => {})
      //sync
      producer.send(data)
    }

    System.out.println("sent per second: " + events * 1000 / (System.currentTimeMillis() - t))
    producer.close()
  }


}