package io.holunda.camunda.taskpool.view.query

/**
 * Query result representing a list of elements and the total number of elements.
 * @param elements elements in the query result.
 * @param totalElementCount in the result before the slice has been applied.
 */
open class QueryResult<T : Any, S : QueryResult<T, S>>(
  val totalElementCount: Int,
  open val elements: List<T>
) {
  constructor(elements: List<T>) : this(totalElementCount = elements.size, elements = elements)

  /**
   * Slices the result.
   * @param query contaning slicing information.
   * @return result having the specified number of elements.
   */
  open fun slice(query: PageableSortableQuery): QueryResult<T, S> {
    val totalCount = this.elements.size
    val offset = query.page * query.size
    return if (totalCount > offset) {
      QueryResult(
        totalElementCount = totalCount,
        elements = this.elements.slice(offset until Math.min(offset + query.size, totalCount))
      )
    } else {
      QueryResult(
        totalElementCount = totalCount,
        elements = this.elements
      )
    }
  }

}

