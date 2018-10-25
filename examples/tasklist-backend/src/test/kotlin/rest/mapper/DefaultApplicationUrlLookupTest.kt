package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.DefaultApplicationUrlLookup
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DefaultApplicationUrlLookupTest {

  private val lookup = DefaultApplicationUrlLookup()

  @Test
  fun `return localhost`() {
    assertThat(lookup.lookup("foo")).isEqualTo("http://localhost:8080/foo")
  }
}
