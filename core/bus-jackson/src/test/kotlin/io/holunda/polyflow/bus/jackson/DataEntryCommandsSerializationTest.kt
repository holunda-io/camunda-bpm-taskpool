package io.holunda.polyflow.bus.jackson

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import io.holunda.camunda.taskpool.api.business.*
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class DataEntryCommandsSerializationTest {
  private val om = jacksonObjectMapper().configurePolyflowJacksonObjectMapper()
    .apply {
    }

  @Test
  @Disabled("fails comparing payload and modification")
  fun `serialize createDataEntryCommand`() {
    val cmd = CreateDataEntryCommand(
      dataEntryChange = DataEntryChange(
        entryType = "order",
        entryId = "12345",
        type = "purchase order",
        applicationName = "someApp",
        name = "Order 12345",
        correlations = newCorrelations().putValueTyped("foo", stringValue("bar")),
        payload = Variables.createVariables()
          .putValue("long",17L)
          .putValue("boolean", true),
        description = "some text",
        state = ProcessingType.IN_PROGRESS.of("created"),
        modification = Modification.NONE
      )
    )
    val json = om.writeValueAsString(cmd)
    val deserialized = om.readValue<CreateDataEntryCommand>(json)

    assertThat(deserialized)
      .usingRecursiveComparison()
      .isEqualTo(cmd)
  }
}
