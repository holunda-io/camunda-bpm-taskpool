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
 * @param involvementsOnly controls of the result should contain only data entries where the `user` was involved
 * @param page current page, zero-based index.
 * @param size page size.
 * @param sort sort of data entries.
 * @param filters list of filters.
 */
data class DataEntriesForUserQuery(
  val user: User,
  val involvementsOnly: Boolean = false,
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: List<String> = listOf(),
  val filters: List<String> = listOf()

) : FilterQuery<DataEntry>, PageableSortableQuery {

  @Deprecated("Please use other constructor setting sort as List<String>")
  constructor(user: User, page: Int = 0, size: Int = Int.MAX_VALUE, sort: String?, filters: List<String> = listOf()): this(
    user = user,
    page = page,
    size = size,
    sort = if (sort.isNullOrBlank()) {
      listOf()
    } else {
      listOf(sort)
    },
    filters = filters
  )

  constructor(user: User, page: Int = 0, size: Int = Int.MAX_VALUE, sort: List<String>, filters: List<String> = listOf()): this(
    user = user,
    involvementsOnly = false,
    page = page,
    size = size,
    sort = sort,
    filters = filters
  )

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
      && (if (involvementsOnly) element.protocol.map { it.username }.contains(this.user.username) else true)
}
