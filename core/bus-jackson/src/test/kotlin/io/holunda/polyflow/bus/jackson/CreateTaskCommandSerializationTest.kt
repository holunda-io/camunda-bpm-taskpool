package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.GenericReference
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test

internal class CreateTaskCommandSerializationTest {
  private val om = jacksonObjectMapper().configurePolyflowJacksonObjectMapper()

  @Test
  fun `serialize createTaskCommand from to json`() {
    val cmd = CreateTaskCommand(
      id = "1",
      sourceReference = GenericReference(
        "1",
        "1",
        "def#1",
        "def",
        "name",
        "appName",
        "tenant"
      ),
      taskDefinitionKey = "taskKey",
      formKey = "form",
      businessKey = "123",
      payload = Variables.putValueTyped("foo", Variables.stringValue("bar"))
    )

    val json = om.writeValueAsString(cmd)

    val deserialized = om.readValue<CreateTaskCommand>(json)

    assertThat(deserialized).isEqualTo(cmd)
  }
}
