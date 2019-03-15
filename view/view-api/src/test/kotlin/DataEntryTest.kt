package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.newCorrelations
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DataEntryTest {

  @Test
  fun `has identity`() {
    assertThat(DataEntry("type", "foo", newCorrelations()).identity).isEqualTo("type#foo")
  }
}
