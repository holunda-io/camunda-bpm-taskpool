package io.holunda.camunda.taskpool.view.query

/**
 * Query result representing a list of elements and the total number of elements.
 */
open class QueryResult<T : Any, S : QueryResult<T, S>>(
  val totalElementCount: Int,
  open val elements: List<T>
) {
  constructor(elements: List<T>) : this(elements.size, elements)

  /**
   * Slices a result.
   */
  open fun slice(query: PageableSortableQuery): QueryResult<T, S> {
    val totalCount = this.elements.size
    val offset = query.page * query.size
    return if (totalCount > offset) {
      QueryResult(totalElementCount = totalCount, elements = this.elements.slice(offset until Math.min(offset + query.size, totalCount)))
    } else {
      QueryResult(totalElementCount = totalCount, elements = this.elements)
    }
  }

}

