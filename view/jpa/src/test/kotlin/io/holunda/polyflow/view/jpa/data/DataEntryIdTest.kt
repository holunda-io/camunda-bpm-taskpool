package io.holunda.polyflow.view.jpa.data

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

internal class DataEntryIdTest {

  @Test
  fun `should construct data entry id `() {
    val id = DataEntryId("type:id")
    assertThat(id).isEqualTo(DataEntryId(entryType = "type", entryId = "id"))
  }


  @Test
  fun `should not construct data entry id`() {
    assertThatThrownBy { DataEntryId("bad string") }.hasMessage("Illegal identity format, expecting <entryType>:<entryId>")
  }


}
