package io.holunda.polyflow.view.jpa.data

import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal.Companion.user
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

    val specification = criteria.toSpecification()
    assertThat(specification).isNotNull
  }

  @Test
  internal fun `should create multiple attribute specification`() {
    val filters = listOf("data.state.state=In Progress", "data.state.processingType=IN_PROGRESS")
    val criteria = toCriteria(filters)

    val specification = criteria.toSpecification()
    assertThat(specification).isNotNull
  }

  @Test
  internal fun `should create multiple principal specifications`() {
    val spec = DataEntryRepository.Companion.isAuthorizedFor(setOf(
      user("kermit"),
      group("muppets"),
      group("avengers"),
    ))

    assertThat(spec).isNotNull
  }

}
