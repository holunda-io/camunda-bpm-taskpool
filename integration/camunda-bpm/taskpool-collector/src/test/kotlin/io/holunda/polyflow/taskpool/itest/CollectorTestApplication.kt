package io.holunda.polyflow.taskpool.itest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.polyflow.bus.jackson.config.FallbackPayloadObjectMapperAutoConfiguration
import io.holunda.polyflow.taskpool.EnableCamundaTaskpoolCollector
import io.holunda.polyflow.taskpool.sender.task.accumulator.ProjectingCommandAccumulator
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.engine.impl.cfg.IdGenerator
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.camunda.spin.spi.DataFormatConfigurator
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.lang.IllegalArgumentException


@SpringBootApplication
@EnableProcessApplication
@EnableCamundaTaskpoolCollector
class CollectorTestApplication {

  @Bean
  @Primary
  fun testAxonCommandGateway(): CommandGateway = mock()

  @Bean
  fun objectMapper(): ObjectMapper = jacksonObjectMapper()

  @Primary
  @Bean
  fun myTaskCommandAccumulator(@Qualifier(FallbackPayloadObjectMapperAutoConfiguration.PAYLOAD_OBJECT_MAPPER) objectMapper: ObjectMapper) =
    ProjectingCommandAccumulator(
      objectMapper = objectMapper,
      serializePayload = true,
      simpleIntentDetectionBehaviour = false
    )
}

class JacksonDataFormatConfigurator : DataFormatConfigurator<JacksonJsonDataFormat> {

  override fun configure(dataFormat: JacksonJsonDataFormat) {
    val objectMapper = dataFormat.objectMapper
    objectMapper.registerModule(KotlinModule.Builder().build())
  }

  override fun getDataFormatClass(): Class<JacksonJsonDataFormat> {
    return JacksonJsonDataFormat::class.java
  }
}

class TestBatchingIdGenerator(private val batchSize: Int): IdGenerator {

  private val idGenerator = StrongUuidGenerator()
  private val ids: MutableList<String> = mutableListOf()
  private var currentIdIndex: Int = 0

  override fun getNextId(): String {
    if (currentIdIndex >= ids.size) {
      generateIds()
    }
    return ids[currentIdIndex++]
  }

  fun getId(index: Int): String {
    return ids.getOrNull(index) ?: throw IllegalArgumentException("Only ${ids.size} are available, you requested $index")
  }

  fun getCurrentIdIndex() = currentIdIndex

  private fun generateIds() {
    for (i in 1..batchSize) {
      ids.add(idGenerator.nextId)
    }
  }

}
