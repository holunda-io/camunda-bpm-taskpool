package io.holunda.polyflow.view.query.data

import io.holunda.polyflow.view.DataEntry
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.jupiter.api.Test

internal class DataEntriesQueryTest {

  private val dataEntry = DataEntry(
    entryType = "io.type",
    entryId = "0239480234",
    type = "data entry",
    applicationName = "test-application",
    name = "Data Entry for case 4711",
    payload = createVariables().apply {
      put("case", "4711")
      put("other", "SADFSA")
    }
  )

  @Test
  fun `should evaluate filter`() {
    assertThat(DataEntriesQuery(filters = listOf()).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesQuery(filters = listOf("case=4711")).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesQuery(filters = listOf("data.type=data entry")).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesQuery(filters = listOf("data.type=other type")).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesQuery(filters = listOf("cas=4711")).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesQuery(filters = listOf("case=4712")).applyFilter(dataEntry)).isFalse
  }
}
