package com.tencent.cloud.emr

import java.net.{URI, URLDecoder}
import java.util.Date

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang.time.{DateFormatUtils, DateUtils}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by liubangchen on 2017/11/21.
  * spark-submit  --class  com.tencent.cloud.emr.LogParser ./emr-example-1.0-SNAPSHOT.jar
  */
object LogParser {

  def getLogTime(filename: String): (Date) = {
    val d = filename.substring(0, 12)
    return DateUtils.parseDate(d, Array[String] {
      "yyyyMMddHHmm"
    })
  }

  def inTimeRange(start: Date, end: Date, dateval: Date): (Boolean) = {
    if (dateval.after(start) && dateval.before(end)) {
      return true
    }
    return false
  }


  def parseData(str: String, sc: SparkContext): (RDD[String]) = {
    val data: Array[String] = new Array[String](2)
    val tmpary = str.split(" ")
    val urlinfo = tmpary(8)
    val index = urlinfo.indexOf("?")
    val url = urlinfo.substring(0, index)
    val params = urlinfo.substring(index + 1, urlinfo.length)

    var paramsmap = params.split("&").map(v => {
      val m = v.split("=", 2).map(s => URLDecoder.decode(s, "UTF-8"))
      m(0) -> m(1)
    }).toMap
    paramsmap += ("url" -> url)
    val mapper = new ScalaObjectMapper()
    data(0) = tmpary(0)
    data(1) = mapper.writeValueAsString(paramsmap)
    return sc.makeRDD(data)
  }

  def main(args: Array[String]): Unit = {
    val conf = new Configuration();
    //conf.addResource(new Path("file:///usr/local/service/hadoop/etc/hadoop/core-site.xml"))
    //conf.addResource(new Path("file:///usr/local/service/hadoop/etc/hadoop/hdfs-site.xml"))
    val fileSystem = FileSystem.get(new URI("cosn://hadoopshanghai/"), conf)
    val starttime = DateUtils.parseDate("2017-11-2117:05", Array[String] {
      "yyyy-MM-ddHH:mm"
    })
    val now = new Date();
    val dateval = DateFormatUtils.format(now, "yyyy/MM/dd")
    val rootdir = "cosn://hadoopshanghai/nginx_log/pv/2017/11/21"
    val filestatus = fileSystem.listStatus(new Path(rootdir))

    var acceptfiles: ArrayBuffer[String] = ArrayBuffer[String]()

    for (i <- 0 until filestatus.length) {
      val fs = filestatus(i)
      val filename = fs.getPath().getName
      val logtime = getLogTime(filename)
      val fullpath = rootdir + "/" + filename
      if (inTimeRange(starttime, now, logtime)) {
        acceptfiles += fullpath
      }
    }

    val sparkconf = new SparkConf().setAppName("sparkStreamingTest")
    val sc = new SparkContext(sparkconf)
    for (i <- 0 until (acceptfiles.length)) {
      val fullpath = acceptfiles(i)
      val rdd = sc.textFile(fullpath)
      rdd.take(1).map(v => {
        parseData(v, sc)
      }).foreach(arg => {
        arg.foreach(v => {
          println(v)
        })
      })
      println(fullpath)
    }

    fileSystem.close()
  }
}
