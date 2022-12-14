package io.holunda.polyflow.taskpool.sender

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EngineTaskCommandsSendingJobHandlerConfigurationTest {

  private val objectMapper = jacksonObjectMapper()

  @Test
  fun `serializes configuration`() {
    val config = EngineTaskCommandsSendingJobHandlerConfiguration(
      taskId = "4711",
      commandByteArrayId = "0815"
    )

    val json = objectMapper.writeValueAsString(config)
    assertThat(json).isEqualTo("""{"taskId":"4711","commandByteArrayId":"0815"}""")

    val back: EngineTaskCommandsSendingJobHandlerConfiguration = objectMapper.readValue(json)
    assertThat(back).isEqualTo(config)
  }
}