package com.tencent.cloud.emr

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
  * Created by liubangchen on 2017/11/21.
  */
class ScalaObjectMapper extends ObjectMapper {

  {
    registerModule(DefaultScalaModule)
  }
}
