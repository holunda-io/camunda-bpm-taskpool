package io.holunda.polyflow.taskpool.sender.task.accumulator

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.polyflow.taskpool.configureTaskpoolJacksonObjectMapper
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * The property operation defines how a new value can be applied to existing value.
 * values a map of values
 * key the key to access the value
 * value the new value
 */
typealias PropertyOperation = (values: MutableMap<String, Any?>, key: String, value: Any?) -> Unit

/**
 * Property operation config.
 * Configures the property operations per class.
 */
typealias PropertyOperationConfiguration = Map<KClass<out Any>, PropertyOperation>

/**
 * Reads a command and returns its properties as a map.
 */
typealias Mapper<T> = (value: T) -> MutableMap<String, Any?>

/**
 * Reads a map and returns a command.
 */
typealias Unmapper<T> = (map: MutableMap<String, Any?>) -> T

/**
 * Flattens the changes from detail objects into the original by applying property operation configuration.
 */
fun <T : Any> projectProperties(
  original: T,
  details: List<Any> = emptyList(),
  propertyOperationConfig: PropertyOperationConfiguration = mapOf(),
  mapper: Mapper<T>,
  unmapper: Unmapper<T>,
  ignoredProperties: List<String> = emptyList(),
  projectionErrorDetector: ProjectionErrorDetector
): T {

  val originalProperties = original.javaClass.kotlin.memberProperties
  // read original into a map
  val values: MutableMap<String, Any?> = mapper.invoke(original)

  for (detail in details) {

    // the property could be taken in consideration if and only if
    // - a property with the same name exists in the original
    // - it is not ignored
    val potentialMatchingProperties = detail.javaClass.kotlin.memberProperties.filter { detailProperty ->
      // not ignored
      !ignoredProperties.contains(detailProperty.name) &&
        originalProperties.any {
          it.name == detailProperty.name
        }
    }

    if (potentialMatchingProperties.isEmpty()) {
      val errorText = "PROJECTOR-001: No matching attributes of two commands to the same task found. The second command $detail is ignored.";
      if (projectionErrorDetector.shouldReportError(original = original, detail = detail)) {
        LoggerFactory
          .getLogger(ProjectingCommandAccumulator::class.java)
          .error(errorText)
      } else {
        LoggerFactory
          .getLogger(ProjectingCommandAccumulator::class.java)
          .debug(errorText)
      }
    }

    //
    // the property should be taken in consideration if and only if
    // - and either a property with the same return type exists in the original and the value of the property differs from that in original
    // - or a property is a collection or a map
    val matchingProperties = potentialMatchingProperties.filter { detailProperty ->
      originalProperties.any {
        it.returnType == detailProperty.returnType && it.get(original) != detailProperty.get(detail)
          || (detailProperty.get(detail) is MutableMap<*, *> || detailProperty.get(detail) is MutableCollection<*>)
      }
    }

    // determine property operation
    val propertyOperation = propertyOperationConfig.getOrDefault(detail.javaClass.kotlin) { map, key, value -> map[key] = value }

    // store values in a map
    matchingProperties.forEach { matchingProperty ->
      propertyOperation(values, matchingProperty.name, matchingProperty.get(detail))
    }
  }
  // write back
  return unmapper.invoke(values)
}

/**
 * Default Jackson Mapper (object to map).
 */
fun <T> jacksonMapper(objectMapper: ObjectMapper = jacksonObjectMapper()): Mapper<T> = {
  objectMapper
    .configureTaskpoolJacksonObjectMapper()
    .convertValue(it, object : TypeReference<MutableMap<String, Any?>>() {})
}

/**
 * Default Jackson Unmapper (map to object).
 */
fun <T> jacksonUnmapper(clazz: Class<T>, objectMapper: ObjectMapper = jacksonObjectMapper()): Unmapper<T> = {
  objectMapper
    .configureTaskpoolJacksonObjectMapper()
    .convertValue(it, clazz)
}

/**
 * Detector of errors during projection.
 */
interface ProjectionErrorDetector {
  /**
   * Determines if an error will be reported.
   * @param original main command.
   * @param detail detail properties.
   * @return true if by property mismatch an error whsould be reported.
   */
  fun shouldReportError(original: Any, detail: Any): Boolean = true
}
