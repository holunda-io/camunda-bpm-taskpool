package io.holunda.camunda.taskpool.view

import org.assertj.core.api.Assertions.assertThat
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
}
