package io.holunda.polyflow.taskpool.sender

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EngineTaskCommandsByteSerializationTest {

  private val objectMapper = jacksonObjectMapper().configurePolyflowJacksonObjectMapper().apply {
    activateDefaultTyping(
      LaissezFaireSubTypeValidator(),
      ObjectMapper.DefaultTyping.EVERYTHING,
      JsonTypeInfo.As.WRAPPER_ARRAY
    )
  }
  private val testObjectFactory = TestObjectFactory()

  @Test
  fun `serializes commands forth and deserializes them back`() {

    val commands = listOf(
      testObjectFactory.createCommand(name = "hello")
    )

    val bytes: ByteArray = objectMapper.writeValueAsBytes(commands)

    val restoredCommands: List<EngineTaskCommand> =
      objectMapper.readValue(bytes, objectMapper.typeFactory.constructCollectionLikeType(List::class.java, EngineTaskCommand::class.java))

    assertThat(restoredCommands).containsExactlyInAnyOrderElementsOf(commands)
  }


}