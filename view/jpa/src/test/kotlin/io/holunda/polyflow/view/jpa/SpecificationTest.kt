package io.holunda.polyflow.view.jpa

import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.DataEntryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Sort.Direction
import java.lang.NumberFormatException

/**
 * Tests conversion of criteria into JPA Specifications.
 */
internal class SpecificationTest {

  @Test
  internal fun `creates single attribute specification`() {
    val filters = listOf("data.state.state=In Progress")
    val criteria = toCriteria(filters)

    val specification = criteria.toDataEntrySpecification(polyflowJpaViewProperties.includeCorrelatedDataEntriesInDataEntryQueries)
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

    val specification = criteria.toDataEntrySpecification(polyflowJpaViewProperties.includeCorrelatedDataEntriesInDataEntryQueries)
    assertThat(specification).isNotNull
  }

  @Test
  fun `creates multiple attribute specification for tasks`() {
    val filters = listOf(
      "task.priority=50",
      "task.businessKey=business-1"
    )
    val criteria = toCriteria(filters)

    val specification = criteria.toTaskSpecification()
    assertThat(specification).isNotNull
  }

  @Test
  fun `fails with unsupported task attribute in filters`() {
    val filters = listOf(
      "task.candidateUsers=foo"
    )
    val criteria = toCriteria(filters)

    val error = assertThrows<IllegalArgumentException> {
     criteria.toTaskSpecification()
    }

    assertThat(error.message).isEqualTo("JPA View found unsupported task attribute for equals comparison: candidateUsers.")
  }

  @Test
  fun `fails with wrong value type for filter attribute`() {
    val filters = listOf(
      "task.priority=foo"
    )
    val criteria = toCriteria(filters)

    val error = assertThrows<NumberFormatException> {
      criteria.toTaskSpecification()
    }

    assertThat(error.message).isEqualTo("For input string: \"foo\"")
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
    val requestAsc = pageRequest(page = 15, size = 42, "+name")
    assertThat(requestAsc.pageNumber).isEqualTo(15)
    assertThat(requestAsc.pageSize).isEqualTo(42)
    assertThat(requestAsc.sort.isSorted).isEqualTo(true)
    assertThat(requestAsc.sort.isEmpty).isEqualTo(false)
    assertThat(requestAsc.sort.getOrderFor("name")!!.direction).isEqualTo(Direction.ASC)

    val requestDesc = pageRequest(page = 15, size = 42, "-name")
    assertThat(requestDesc.pageNumber).isEqualTo(15)
    assertThat(requestDesc.pageSize).isEqualTo(42)
    assertThat(requestDesc.sort.isSorted).isEqualTo(true)
    assertThat(requestDesc.sort.isEmpty).isEqualTo(false)
    assertThat(requestDesc.sort.getOrderFor("name")!!.direction).isEqualTo(Direction.DESC)

    val unsorted = pageRequest(page = 14, size = 41, null)
    assertThat(unsorted.pageNumber).isEqualTo(14)
    assertThat(unsorted.pageSize).isEqualTo(41)
    assertThat(unsorted.sort.isSorted).isEqualTo(false)
    assertThat(unsorted.sort.isEmpty).isEqualTo(true)

  }
}
