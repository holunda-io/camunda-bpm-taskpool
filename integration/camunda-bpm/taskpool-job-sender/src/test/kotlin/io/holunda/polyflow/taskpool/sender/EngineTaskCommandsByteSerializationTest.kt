package io.holunda.polyflow.taskpool.sender

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EngineTaskCommandsByteSerializationTest {

  private val objectMapper = CamundaJobSenderConfiguration(SenderProperties()).fallbackCommandByteArrayObjectMapper()
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
