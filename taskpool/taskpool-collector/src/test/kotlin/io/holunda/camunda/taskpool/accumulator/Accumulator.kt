@file:Suppress("UNREACHABLE_CODE")

package io.holunda.camunda.taskpool.accumulator

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*
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
fun <T : Any> flatten(original: T, details: List<Any> = emptyList(), opContainerConfig: ContainerOperationConfiguration = mapOf()): T {

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
    matchingProperties.forEach {
      mapOperation(values, it.name, it.get(detail)!!)
    }
  }

  // write back
  return jacksonObjectMapper().convertValue(values, original::class.java)
}

class FlattenTest {

  @Test
  fun `should return the command if details are empty`() {

    val model = model("Foo", "My foo model")
    val result = flatten(model)

    assertThat(result).isEqualTo(model)
  }

  @Test
  fun `should replace a detail`() {

    val model = model("Foo", "My foo model")
    val result = flatten(model, listOf(named(name = "new name", id = model.id)))

    assertThat(result).isEqualTo(model.copy(name = "new name"))
  }

  @Test
  fun `should replace a detail with the last value`() {

    val model = model("Foo", "My foo model")
    val result = flatten(model, listOf(
      named(name = "wrong name", id = model.id),
      named(name = "new name", id = model.id))
    )

    assertThat(result).isEqualTo(model.copy(name = "new name"))
  }

  @Test
  fun `should replace detail map`() {

    val model = model("Foo", "My foo model", payload = mutableMapOf("foo" to "bar"))
    val result = flatten(model, listOf(
      payload(payload = mutableMapOf("zee" to "test"), id = model.id),
      named(name = "new name", id = model.id))
    )

    assertThat(result).isEqualTo(model.copy(name = "new name", enriched = true, payload = mutableMapOf("zee" to "test")))
  }

  @Test
  fun `should add a detail to a map`() {

    val model = model("Foo", "My foo model", payload = mutableMapOf("foo" to "bar"))
    val result = flatten(model, listOf(
      payload(payload = mutableMapOf("zee" to "test"), id = model.id),
      named(name = "new name", id = model.id)),
      mutableMapOf<KClass<out Any>, ContainerOperation>().apply {
        put(Payload::class) { map, key, value ->
          val originalValue = map[key]
          when (originalValue) {
            is MutableMap<*, *> -> originalValue.putAll(value as Map<Nothing, Nothing>)
            else -> map[key] = value
          }
        }
      }
    )

    assertThat(result).isEqualTo(model.copy(name = "new name", enriched = true, payload = mutableMapOf("foo" to "bar", "zee" to "test")))
  }


  @Test
  fun `should remove a detail from a map`() {

    val model = model("Foo", "My foo model", payload = mutableMapOf("foo" to "bar"), users = mutableListOf("kermit", "gonzo"))
    val result = flatten(model, listOf(

      // use payload
      payload(payload = mutableMapOf("foo" to "bar"), id = model.id, users = mutableListOf("gonzo")),

      named(name = "new name", id = model.id)),

      // to remove elements
      mutableMapOf<KClass<out Any>, ContainerOperation>().apply {
        put(Payload::class) { map, key, value ->
          val originalValue = map[key]
          when (originalValue) {
            is MutableMap<*, *> -> (value as Map<*, *>).entries.forEach { originalValue.remove(key = it.key, value = it.value) }
            is MutableCollection<*> -> (value as MutableCollection<*>).forEach { originalValue.remove(it) }
            else -> map[key] = value
          }
        }
      }
    )

    // map elements should vanish, kermit remains alone in the list
    assertThat(result).isEqualTo(model.copy(name = "new name", enriched = true, payload = mutableMapOf(), users = mutableListOf("kermit")))
  }

}

internal fun model(
  name: String, description: String = "",
  id: String = UUID.randomUUID().toString(),
  payload: MutableMap<String, Any> = mutableMapOf(),
  users: MutableList<String> = mutableListOf()
) = Model(
  id = id,
  name = name,
  description = description,
  payload = payload,
  users = users,
  enriched = !payload.isEmpty()
)

internal fun payload(payload: MutableMap<String, Any>, id: String = UUID.randomUUID().toString(), users: MutableList<String> = mutableListOf()) = Payload(
  id = id,
  payload = payload,
  users = users
)


internal fun named(name: String, id: String = UUID.randomUUID().toString()) = NamedWithId(id = id, name = name)


internal data class Model(
  override val id: String,
  val name: String,
  val description: String,
  override var enriched: Boolean = false,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override val users: MutableList<String> = mutableListOf()
) : WithId, WithPayload, WithUsers

internal data class NamedWithId(
  override val id: String,
  val name: String
) : WithId

internal data class Payload(
  override val id: String,
  override var enriched: Boolean = true,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override val users: MutableList<String> = mutableListOf()

) : WithId, WithPayload, WithUsers

internal interface WithId {
  val id: String
}

internal interface WithPayload {
  val enriched: Boolean
  val payload: MutableMap<String, Any>
}

internal interface WithUsers {
  val users: MutableList<String>
}
