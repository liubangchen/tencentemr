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
    conf.set("spark.hadoop.validateOutputSpecs", "false")
    val spark = new SparkContext(conf)
    var rdds = spark.textFile(input)

    var outputrdd = spark.emptyRDD[String]

    var totalrdds = rdds.filter(line => line.split(",").size >= 3).map(line => {
      val linewords = line.split(",")
      var key = ""
      var value = (0: Long, 0: Long) //统计值，实际值
      if (linewords(2).length > 18) {
        key = linewords(linewords.size - 2)
        value = (0, 1)
      } else {
        var v: Long = 0
        try {
          v = linewords(2).toLong
        } catch {
          case e: Exception => {
            v = 0
          }
        }
        key = linewords(0)
        value = (v, 0)
      }
      (key, value)
    }).reduceByKey((v1, v2) => {
      (v1._1 + v2._1, v1._2 + v2._2)
    }).saveAsTextFile(output)

  }
}
