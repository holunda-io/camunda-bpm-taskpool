package io.holunda.polyflow.view

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test

class DataEntryTest {

  @Test
  fun `has identity`() {
    assertThat(DataEntry(
      entryType = "type",
      entryId = "foo",
      name = "some",
      applicationName = "app1",
      type = "myType"
    ).identity).isEqualTo("type#foo")
  }


  // see https://github.com/holunda-io/camunda-bpm-taskpool/issues/609
  @Test
  fun `can serialize and deserialize datEntry with jackson`() {
    val dataEntry = DataEntry(entryType = "A", entryId = "1", payload = Variables.putValue("x", "y"), name = "A1", type = "A", applicationName = "y")

    val om = jacksonObjectMapper().configurePolyflowJacksonObjectMapper()

    val json = om.writeValueAsString(dataEntry)

    val deserialized = om.readValue<DataEntry>(json)

    assertThat(deserialized).isEqualTo(dataEntry)
  }
}
