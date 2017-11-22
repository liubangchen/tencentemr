package com.tencent.cloud.emr.modle

/**
  * Created by liubangchen on 2017/11/22.
  */
class Requestinfo(
                   c: String,
                   cy: String,
                   addr: String,
                   apid: String,
                   mb: String,
                   im: String,
                   puid: String
                 ) {
  var city: String = c
  var country: String = cy
  var ip: String = addr
  var appid: String = apid
  var mobile: String = mb
  var imsi: String = im
  var pvuid: String = puid
}
