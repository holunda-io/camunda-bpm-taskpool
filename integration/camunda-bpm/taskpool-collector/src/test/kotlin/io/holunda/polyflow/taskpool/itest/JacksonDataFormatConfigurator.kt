package io.holunda.polyflow.taskpool.itest

import tools.jackson.module.kotlin.KotlinModule
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.camunda.spin.spi.DataFormatConfigurator

class JacksonDataFormatConfigurator : DataFormatConfigurator<JacksonJsonDataFormat> {

  override fun configure(dataFormat: JacksonJsonDataFormat) {
    val objectMapper = dataFormat.objectMapper
    objectMapper.registerModule(KotlinModule.Builder().build())
  }

  override fun getDataFormatClass(): Class<JacksonJsonDataFormat> {
    return JacksonJsonDataFormat::class.java
  }
}