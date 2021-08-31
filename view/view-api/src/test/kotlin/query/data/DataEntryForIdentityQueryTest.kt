package io.holunda.polyflow.view.query.data

import io.holunda.polyflow.view.DataEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class DataEntryForIdentityQueryTest {

  private val dataEntry = DataEntry(
    entryType = "io.type",
    entryId = "0239480234",
    type = "data entry",
    applicationName = "test-application",
    name = "Data Entry for case 4711",
  )

  @Test
  fun `should filter by identity`() {
    assertThat(DataEntryForIdentityQuery(entryType = "io.type", entryId = "0239480234").applyFilter(dataEntry)).isTrue
    assertThat(DataEntryForIdentityQuery(entryType = "other.type", entryId = "0239480234").applyFilter(dataEntry)).isFalse
    assertThat(DataEntryForIdentityQuery(entryType = "io.type", entryId = "other-id").applyFilter(dataEntry)).isFalse
  }
}
