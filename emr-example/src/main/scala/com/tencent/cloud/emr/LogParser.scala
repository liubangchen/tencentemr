package com.tencent.cloud.emr

import java.net.{URI, URLDecoder}
import java.util.Date
import com.tencent.cloud.emr.modle.Requestinfo
import org.apache.commons.lang.time.{DateFormatUtils, DateUtils}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.{SaveMode, SparkSession}
import scala.collection.mutable.ArrayBuffer

/**
  * Created by liubangchen on 2017/11/21.
  * spark-submit  --class  com.tencent.cloud.emr.LogParser ./emr-example-1.0-SNAPSHOT.jar 2017-11-2120:10
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

  def decoderValue(v: String): (String) = {
    try {
      URLDecoder.decode(v, "UTF-8")
    } catch {
      case e: Exception => {
        v
      }
    }
  }

  def parseData(str: String, ds: String, session: SparkSession): (String) = {
    val tmpary = str.split(" ")
    val urlinfo = tmpary(8)
    val index = urlinfo.indexOf("?")
    if (index < 0) {
      return "{}"
    }
    val url = urlinfo.substring(0, index)
    val params = urlinfo.substring(index + 1, urlinfo.length)
    val mapper = new ScalaObjectMapper()
    var paramsmap: Map[String, String] = Map()
    try {
      paramsmap = params.split("&").map(v => {
        var value = v
        if (v.indexOf("=") < 0) {
          value = v + "= "
        }
        val m = value.split("=", 2).map(s => decoderValue(s))
        m(0) -> m(1)
      }).toMap

    } catch {
      case e: Exception => {
        println(params)
        throw e
      }
    }
    var req = new Requestinfo(paramsmap.get("city").getOrElse(""),
      paramsmap.get("country").getOrElse(""),
      paramsmap.get("ip").getOrElse(""),
      paramsmap.get("appid").getOrElse(""),
      paramsmap.get("mobile").getOrElse(""),
      paramsmap.get("imsi").getOrElse(""),
      paramsmap.get("pvuid").getOrElse("")
    )

    return mapper.writeValueAsString(req) //sc.makeRDD(mapper.writeValueAsString(paramsmap))
  }

  def main(args: Array[String]): Unit = {
    val conf = new Configuration();
    if (args == null || args.length <= 0) {
      println("please input time")
      System.exit(0)
    }
    val fileSystem = FileSystem.get(new URI("cosn://hadoopshanghai/"), conf)
    val starttime = DateUtils.parseDate(args(0), Array[String] {
      "yyyy-MM-ddHH:mm"
    })
    val now = new Date();
    val dateval = DateFormatUtils.format(now, "yyyy/MM/dd")
    val ds = DateFormatUtils.format(now, "yyyyMMdd")
    val rootdir = "cosn://hadoopshanghai/nginx_log/pv/" + dateval
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

    var sparksession = SparkSession.builder().appName("logparse").
      config("hive.exec.dynamic.partition.mode", "nonstrict").
      enableHiveSupport().
      getOrCreate();
    var allrdd = sparksession.sparkContext.emptyRDD[String]
    var bds = sparksession.sparkContext.broadcast[String](ds)


    for (i <- 0 until (acceptfiles.length)) {
      val fullpath = acceptfiles(i)
      val rdd = sparksession.read.textFile(fullpath).rdd
      var jsonrdd = rdd.map(v => {
        parseData(v, bds.value, sparksession)
      })
      allrdd = allrdd.union(jsonrdd)
    }
    val df = sparksession.read.json(allrdd)
    df.repartition(1).write.mode(SaveMode.Append).format("ORC").saveAsTable("requestinfo_tmp")
    var insertsql: String = "insert into table requestinfo partition (ds='" + bds.value + "') select * from requestinfo_tmp"
    sparksession.sql(insertsql)
    sparksession.sql("drop table requestinfo_tmp")
    fileSystem.close()
  }
}
