package io.holunda.polyflow.view.query.data

import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.filter.createDataEntryPredicates
import io.holunda.polyflow.view.filter.filterByPredicate
import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Queries data entries for provided user.
 * @param user user authorized to access data entries.
 * @param page current page.
 * @param size page size.
 * @param sort sort of data entries.
 * @param filters list of filters.
 */
data class DataEntriesForUserQuery(
  val user: User,
  override val page: Int = 1,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  val filters: List<String> = listOf()

) : FilterQuery<DataEntry>, PageableSortableQuery {

  // jackson serialization works because delegate property is private
  private val predicates by lazy { createDataEntryPredicates(toCriteria(filters)) }

  override fun applyFilter(element: DataEntry): Boolean =
    // filter
    filterByPredicate(element, predicates) && (
      // authorized users
      element.authorizedUsers.contains(this.user.username)
        // authorized groups
        || (element.authorizedGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) })
      )
}
