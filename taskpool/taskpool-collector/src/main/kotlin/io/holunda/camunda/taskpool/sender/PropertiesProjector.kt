package io.holunda.camunda.taskpool.sender

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.variable.VariableMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * The property operation defines how a new value can be applied to existing value.
 * @param values a map of values
 * @param key the key to access the value
 * @param value the new value
 */
typealias PropertyOperation = (values: MutableMap<String, Any?>, key: String, value: Any?) -> Unit

/**
 * Property operation config.
 * Configures the property operations per class.
 */
typealias PropertyOperationConfiguration = Map<KClass<out Any>, PropertyOperation>


typealias Mapper<T> = (value: T) -> MutableMap<String, Any?>
typealias Unmapper<T> = (map: MutableMap<String, Any?>) -> T

/**
 * Flattens the changes from detail objects into the original by applying property operation configuration.
 */
fun <T : Any> projectProperties(
  original: T,
  details: List<Any> = emptyList(),
  propertyOperationConfig: PropertyOperationConfiguration = mapOf(),
  mapper: Mapper<T> = jacksonMapper(),
  unmapper: Unmapper<T> = jacksonUnmapper(original::class.java)
): T {

  val originalProperties = original.javaClass.kotlin.memberProperties
  // read original into a map
  val values: MutableMap<String, Any?> = mapper.invoke(original)

  for (detail in details) {
    val matchingProperties = detail.javaClass.kotlin.memberProperties.filter { detailProperty ->
      // the property should be taken in consideration if and only if
      // - a property with the same name exists in the original
      // - and either a property with the same return type exists in the original and the value of the property differs from that in original
      // - or a property is a collection or a map
      originalProperties.any {
        it.name == detailProperty.name
          && (it.returnType == detailProperty.returnType && it.get(original) != detailProperty.get(detail)
          || (detailProperty.get(detail) is MutableMap<*, *> || detailProperty.get(detail) is MutableCollection<*>))
      }
    }

    // determine property operation
    val propertyOperation = propertyOperationConfig.getOrDefault(detail.javaClass.kotlin) { map, key, value -> map[key] = value }

    // store values in a map
    matchingProperties.forEach { matchingProperty ->
      propertyOperation(values, matchingProperty.name, matchingProperty.get(detail))
    }
  }

  val clonedOriginal = original

  println(values)

  // write back
  return unmapper.invoke(values)
}


fun <T> jacksonMapper(): Mapper<T> = {
  jacksonObjectMapper().convertValue(it, object : TypeReference<Map<String, Any?>>() {})
}

fun <T> jacksonUnmapper(clazz: Class<T>): Unmapper<T> = {
  jacksonObjectMapper()
    .registerModule(
      SimpleModule().apply {
        addDeserializer(VariableMap::class.java, VariableMapDeserializer())
        addDeserializer(SourceReference::class.java, SourceReferenceDeserializer())
      }
    )

    .convertValue(it, clazz)
}

