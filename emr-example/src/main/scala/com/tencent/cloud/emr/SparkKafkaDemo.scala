package com.tencent.cloud.emr

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.LoggerFactory


/**
  *
  */
object SparkKafkaDemo {

  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("rttcount")
    val ssc = new StreamingContext(conf, Seconds(30))
    val topics = Set("netflow")
    val brokers = "100.91.170.97:9092"
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

    val stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    val messages = stream.flatMap {
      x => x._2.split(";")
    }
    ssc.start()
    ssc.awaitTermination()

  }
}