package io.holunda.polyflow.view.jpa

import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.DataEntryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Tests conversion of criteria into JPA Specifications.
 */
internal class SpecificationTest {

  @Test
  internal fun `should create single attribute specification`() {
    val filters = listOf("data.state.state=In Progress")
    val criteria = toCriteria(filters)

    val specification = criteria.toDataEntrySpecification()
    assertThat(specification).isNotNull
  }

  @Test
  internal fun `should create multiple attribute specification`() {
    val filters = listOf(
      "data.state.state=In Progress",
      "data.state.processingType=IN_PROGRESS",
      "data.entryId=1234",
      "data.entryType=Some entry type",
      "data.type=Some type"
    )
    val criteria = toCriteria(filters)

    val specification = criteria.toDataEntrySpecification()
    assertThat(specification).isNotNull
  }

  @Test
  internal fun `should create multiple principal specifications`() {
    val spec = DataEntryRepository.isAuthorizedFor(
      setOf(
        user("kermit"),
        group("muppets"),
        group("avengers"),
      )
    )

    assertThat(spec).isNotNull
  }
}
