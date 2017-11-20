package com.tencent.cloud.emr

import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.util.Random


/**
  * bin/kafka-topics.sh --create --zookeeper 10.0.0.56:2181 --replication-factor 1 --partitions 4 --topic testdataset
  * Created by liubangchen on 2017/11/17.
  * java -classpath ./jars/scala-library-2.11.8.jar:./emr-example-1.0-SNAPSHOT-jar-with-dependencies.jar  com.tencent.cloud.emr.KafakProducer
  */


object KafakProducer {

  def getProps(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers","10.0.0.184:9092")
    props.put("client.id", "aProducerExample")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    return props
  }

  def makeData(ts: Long): (String, String) = {
    return (String.valueOf(ts), "a,123,456,789,12567")
  }

  def main(args: Array[String]): Unit = {
    val topic = "testdataset"
    val props = getProps()

    val producer = new KafkaProducer[String, String](props)
    val t = System.currentTimeMillis()
    while (true) {
      for (i <- Range(1, 1000)) {
        val t = System.currentTimeMillis() + i
        val msg = makeData(t);
        val data = new ProducerRecord[String, String](topic, msg._1, msg._2)
        //async
        //producer.send(data, (m,e) => {})
        //sync
        producer.send(data)
      }
    }
    System.out.println("sent per second: ")
    producer.close()
  }


}
