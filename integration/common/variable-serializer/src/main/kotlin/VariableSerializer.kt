package io.holunda.camunda.variable.serializer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.time.Instant
import java.util.*

/**
 * Serialize variables into a map using provided object mapper.
 * @param payload serialization content
 * @param mapper optional mapper, if not provided, Jackson default mapper will be used.
 */
fun serialize(payload: Any, mapper: ObjectMapper): VariableMap {
  return Variables.createVariables().apply {
    this.putAll(mapper.convertValue(payload, object : TypeReference<Map<String, Any>>() {}))
  }
}

/**
 * Deserializes JSON back into variable map.
 */
fun String?.toPayloadVariableMap(objectMapper: ObjectMapper): VariableMap = Variables.createVariables().apply {
  if (this@toPayloadVariableMap != null) {
    putAll(objectMapper.readValue(this@toPayloadVariableMap, object : TypeReference<Map<String, Any>>() {}))
  }
}

/**
 * Serializes payload as JSON.
 */
fun VariableMap.toPayloadJson(objectMapper: ObjectMapper): String =
  objectMapper.writeValueAsString(this)

/**
 * JSON path filter to match paths.
 */
typealias JsonPathFilterFunction = (path: String) -> Boolean


/**
 * Converts a deep list of pairs representing the payload into a list of pairs of one level keyed by the JSON path and
 * valued by the value. A map structure is not sufficient because there can be multiple (different) values for the same
 * path. For example a pair with a list value of (customer, [foo, bar]) would be converted to the two pairs
 * (customer, foo) and (customer, bar).
 * Note: The initial map might contain primitive types, maps or lists as values.
 * @param limit limit of levels to convert. Defaults to -1 meaning there is no limit.
 * @param filters filter object to identify properties to include into the result.
 */
fun VariableMap.toJsonPathsWithValues(limit: Int = -1, filters: List<Pair<JsonPathFilterFunction, FilterType>> = emptyList()): Set<Pair<String, Any>> {
    return this.entries
        .map { it.toPair() }
        .map { it.toJsonPathWithValue(prefix = "", limit = limit, filter = filters) }
        .flatten().toSet()
}

internal fun Pair<String, Any?>.toJsonPathWithValue(
  prefix: String = "",
  limit: Int = -1,
  filter: List<Pair<JsonPathFilterFunction, FilterType>>
): List<Pair<String, Any>> {
  // level limit check
  val currentLevel = prefix.count { ".".contains(it) }
  if (limit != -1 && currentLevel >= limit) {
    return listOf()
  }
  // compose the path key
  val key = if (prefix == "") {
    this.first
  } else {
    "$prefix.${this.first}"
  }

  val value = this.second
  return if (value != null && value.isPrimitiveType()) {

    // check the filters
    if (!filter.filter { (_, type) -> type == FilterType.EXCLUDE }.all { (filter, _) ->
        filter.invoke(key).not()
      } || (filter.any { (_, type) -> type == FilterType.INCLUDE } && filter.filter { (_, type) -> type == FilterType.INCLUDE }
        .none { (filter, _) ->
          filter.invoke(key)
        })) {
      // found at least one filter that didn't match the key => exclude the key from processing
      return listOf()
    }

    listOf(key to value)
  } else if (value is Map<*, *>) {
    @Suppress("UNCHECKED_CAST")
    (value as Map<String, Any?>).entries
        .map { it.toPair() }
        .map { it.toJsonPathWithValue(key, limit, filter) }
        .flatten()
  } else if (value is List<*>) {
    value.map { (key to it).toJsonPathWithValue(prefix, limit, filter) }.flatten()
  } else {
    // ignore complex objects
    listOf()
  }
}

internal fun Any.isPrimitiveType(): Boolean {
  return when (this) {
    // TODO: ask Jackson for the supported list of types
    is String, is Boolean, is Number, is Int, is Long, is Float, is Date, is Instant -> true
    else -> false
  }
}


