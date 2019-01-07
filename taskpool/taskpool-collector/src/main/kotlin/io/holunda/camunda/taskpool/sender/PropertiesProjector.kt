package io.holunda.camunda.taskpool.sender

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * The container operation defines how a new value can be applied to existing values
 * if the it is a container (MutableMap or MutableList).
 * @param values a map of values
 * @param key the key to access the value
 * @param value the new value
 */
typealias ContainerOperation = (values: MutableMap<String, Any>, key: String, value: Any) -> Unit

/**
 * Container operation config.
 * Configures the container operations per class.
 */
typealias ContainerOperationConfiguration = Map<KClass<out Any>, ContainerOperation>

/**
 * Flattens the changes from detail objects into the original by applying operation container configuration.
 */
fun <T : Any> projectProperties(original: T, details: List<Any> = emptyList(), opContainerConfig: ContainerOperationConfiguration = mapOf()): T {

  val originalProperties = original.javaClass.kotlin.memberProperties
  // read original into a map
  val values: MutableMap<String, Any> = jacksonObjectMapper().convertValue(original, object : TypeReference<Map<String, Any>>() {})

  for (detail: Any in details) {

    val matchingProperties = detail.javaClass.kotlin.memberProperties.filter { detailProperty ->
      // the property should be taken in consideration if and only if
      // - a property with the same type exists in the original
      // - a property with the same return type exists in the original
      // - the value of the property differs from that in original
      originalProperties.any {
        it.name == detailProperty.name
          && it.returnType == detailProperty.returnType
          && (detailProperty.get(detail) is MutableMap<*, *>
          || detailProperty.get(detail) is MutableCollection<*>
          || it.get(original) != detailProperty.get(detail))
      }
    }

    // determine map operation
    val mapOperation = opContainerConfig.getOrDefault(detail.javaClass.kotlin) { map, key, value -> map[key] = value }

    // store values in a map
    matchingProperties.forEach { matchingProperty ->
      // TODO check if !! is required!
      mapOperation(values, matchingProperty.name, matchingProperty.get(detail)!!)
    }
  }

  // write back
  return jacksonObjectMapper().convertValue(values, original::class.java)
}
