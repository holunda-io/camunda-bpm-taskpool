package io.holunda.camunda.datapool.sender.simple

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.datapool.DataEntrySenderProperties
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test
import org.mockito.Mockito.mock

class SimpleDataEntryCommandSenderTest {

  private val testee: SimpleDataEntryCommandSender = SimpleDataEntryCommandSender(
    gateway = mock(CommandGateway::class.java),
    properties = DataEntrySenderProperties(),
    objectMapper = ObjectMapper()
  )

  @Test
  fun `should return the variables map`() {

    val map = Variables
      .createVariables()
      .putValue("key", "value")
      .putValue("another-key", 4711)

    val result = testee.serialize(map)

    assertThat(result).isEqualTo(map)
    map.forEach {
      assertThat(result).containsKey(it.key)
      assertThat(result[it.key]).isEqualTo(it.value)
    }
  }

  @Test
  fun `should transform simple pojo to map`() {

    data class Pojo(
      val key: String,
      val anotherKey: Int
    )

    val pojo = Pojo(key = "value", anotherKey = 4711)

    val result = testee.serialize(pojo)

    assertThat(result["key"]).isEqualTo(pojo.key)
    assertThat(result["anotherKey"]).isEqualTo(pojo.anotherKey)
  }

  @Test
  fun `should transform pojo with lists to map`() {

    data class Pojo(
      val key: String,
      val anotherKey: List<Int>
    )

    val pojo = Pojo(key = "value", anotherKey = listOf(4711, 4712))

    val result = testee.serialize(pojo)

    assertThat(result["key"]).isEqualTo(pojo.key)
    assertThat(result["anotherKey"] as List<*>).containsOnlyElementsOf(pojo.anotherKey)
  }

  @Test
  fun `should transform complex pojo to map of maps`() {

    val pojo2 = Pojo2(keyZUZUZ = "pojo2", children = listOf(Pojo(key = "value1", anotherKey = listOf())))
    val pojo3 = Pojo2(keyZUZUZ = "pojo3", children = listOf(Pojo(key = "value2", anotherKey = listOf())))

    val pojo = Pojo(key = "value", anotherKey = listOf(pojo2, pojo3))

    val result = testee.serialize(pojo)

    assertThat(result["key"]).isEqualTo(pojo.key)

    val children = result["anotherKey"] as List<Map<String, Any>>
    assertThat(children.size).isEqualTo(2)
    assertThat(children[0]).containsKey("keyZUZUZ")
    assertThat(children[0]).containsKey("children")
    assertThat(children[1]).containsKey("keyZUZUZ")
    assertThat(children[1]).containsKey("children")

    assertThat(children[0]["keyZUZUZ"]).isEqualTo("pojo2")
    assertThat(children[1]["keyZUZUZ"]).isEqualTo("pojo3")

  }

}

data class Pojo(
  val key: String,
  val anotherKey: List<Pojo2>
)

data class Pojo2(
  val keyZUZUZ: String,
  var children: List<Pojo> = listOf()
)

