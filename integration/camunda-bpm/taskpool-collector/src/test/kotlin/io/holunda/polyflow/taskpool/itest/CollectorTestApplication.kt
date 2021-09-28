package io.holunda.polyflow.taskpool.itest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.polyflow.taskpool.EnableCamundaTaskpoolCollector
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.camunda.spin.spi.DataFormatConfigurator
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary


@SpringBootApplication
@EnableProcessApplication
@EnableCamundaTaskpoolCollector
class CollectorTestApplication {

  @Bean
  @Primary
  fun testAxonCommandGateway(): CommandGateway = mock()

  @Bean
  fun objectMapper(): ObjectMapper = jacksonObjectMapper()
}

class JacksonDataFormatConfigurator : DataFormatConfigurator<JacksonJsonDataFormat> {

  override fun configure(dataFormat: JacksonJsonDataFormat) {
    val objectMapper = dataFormat.objectMapper
    objectMapper.registerModule(KotlinModule())
  }

  override fun getDataFormatClass(): Class<JacksonJsonDataFormat> {
    return JacksonJsonDataFormat::class.java
  }

}
