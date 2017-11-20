package com.tencent.cloud.emr

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.LoggerFactory


/**
  *bin/spark-submit --master yarn-cluster --num-executors 10 --class com.tencent.cloud.emr.SparkKafkaDemo  ./emr-example-1.0-SNAPSHOT-jar-with-dependencies.jar
  *
  */
object SparkKafkaDemo {

  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("sparkStreamingTest")
    val ssc = new StreamingContext(conf, Seconds(1))
    val topics = Set("testdataset")
    val brokers = "10.0.0.184:9092"
    val kafkaParams = Map[String, Object]("bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "emrtestgroup",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    val data = stream.flatMap(r => {
      r.value().split(",")
    })
    val datamap = data.map(x => (x, 1))
    val wordCounts = datamap.reduceByKey(_ + _)
    wordCounts.print()
    ssc.start()
    ssc.awaitTermination()

  }
}