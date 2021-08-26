package io.holunda.camunda.variable.serializer

/**
 * Filter checking if the path is equals to the provided value.
 */
data class EqualityPathFilter(
  val path: String,
  val type: FilterType
) : JsonPathFilterFunction {

  companion object {
    /**
     * Constructs an equality exclude filter.
     */
    fun eqExclude(path: String) = EqualityPathFilter(path = path, type = FilterType.EXCLUDE).asPair()

    /**
     * Constructs an equality include filter.
     */
    fun eqInclude(path: String) = EqualityPathFilter(path = path, type = FilterType.INCLUDE).asPair()

    /**
     * None filter will deny any attribute.
     */
    fun none() = { _: String -> false } to FilterType.INCLUDE

    /**
     * None filter will accept all attributes.
     */
    fun all() = { _: String -> true } to FilterType.INCLUDE

  }

  override fun invoke(path: String): Boolean = path == this.path

  fun asPair(): Pair<JsonPathFilterFunction, FilterType> {
    return this::invoke to type
  }
}


/**
 * Filter type.
 */
enum class FilterType {
  /**
   * Matching filter describes inclusion.
   */
  INCLUDE,

  /**
   * Matching filter describes exclusion.
   */
  EXCLUDE
}
