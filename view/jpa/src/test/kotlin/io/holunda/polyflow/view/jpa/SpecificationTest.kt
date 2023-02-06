package io.holunda.polyflow.view.jpa

import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.DataEntryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests conversion of criteria into JPA Specifications.
 */
internal class SpecificationTest {

  @Test
  internal fun `creates single attribute specification`() {
    val filters = listOf("data.state.state=In Progress")
    val criteria = toCriteria(filters)

    val specification = criteria.toDataEntrySpecification()
    assertThat(specification).isNotNull
  }

  @Test
  fun `creates multiple attribute specification`() {
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
  fun `creates multiple principal specifications`() {
    val spec = DataEntryRepository.isAuthorizedFor(
      setOf(
        user("kermit"),
        group("muppets"),
        group("avengers"),
      )
    )

    assertThat(spec).isNotNull
  }

  @Test
  fun `creates a paged request`() {
    val request = pageRequest(page = 15, size = 42, "+name")
    assertThat(request.pageNumber).isEqualTo(15)
    assertThat(request.pageSize).isEqualTo(42)
    assertThat(request.sort.isSorted).isEqualTo(true)
    assertThat(request.sort.isEmpty).isEqualTo(false)

    val unsorted = pageRequest(page = 14, size = 41, null)
    assertThat(unsorted.pageNumber).isEqualTo(14)
    assertThat(unsorted.pageSize).isEqualTo(41)
    assertThat(unsorted.sort.isSorted).isEqualTo(false)
    assertThat(unsorted.sort.isEmpty).isEqualTo(true)

  }
}
