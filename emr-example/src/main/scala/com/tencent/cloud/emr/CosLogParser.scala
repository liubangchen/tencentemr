package com.tencent.cloud.emr

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by liubangchen on 2017/12/7.
  */
object CosLogParser {

  def main(args: Array[String]): Unit = {

    if (args == null || args.length != 2) {
      println("please input file path and out file path")
      System.exit(0)
    }

    var input = args(0)
    var output = args(1)

    val conf = new SparkConf().setAppName("cosLogParse")
    val spark = new SparkContext(conf)
    var rdds = spark.textFile(input)
    var totalrdds = spark.emptyRDD[String]

    spark.setCheckpointDir("/spark/checkpoint")

    var allrdd = rdds.filter(line => line.split(",").size >= 3).map(line => {
      var linewords = line.split(",")
      val size = linewords.size
      var k: String = ""
      var v: Long = 0
      if (linewords(2).length > 18) {
        k = linewords(size - 2) + "-R"
        v = 1
      } else {
        k = linewords(0) + "-T"
        v = linewords(2).toLong
      }
      (k, v)
    }).reduceByKey((v1, v2) => {
      v1 + v2
    })

    allrdd.cache()

    var realvalrdd = spark.broadcast(allrdd.filter(v => v._1.split("-")(1).equals("R")).collect().toMap)


    allrdd.filter(v => v._1.split("-")(1).equals("T")).map(item => {
      var aary = item._1.split("-")
      var sh = aary(0)
      var t = aary(1)
      var value = item._2
      var cnt: Long = -1
      if (realvalrdd.value.contains(sh + "-R")) {
        cnt = realvalrdd.value.get(sh + "-R").get
      }
      (sh, value + ":" + cnt)
    }).saveAsTextFile(output)
  }
}
