import io.holunda.camunda.variable.serializer.serialize
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test

class SimpleDataEntryCommandSenderTest {

  @Test
  fun `should return the variables map`() {

    val map = Variables
      .createVariables()
      .putValue("key", "value")
      .putValue("another-key", 4711)

    val result = serialize(map)

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

    val result = serialize(pojo)

    assertThat(result[Pojo::key.name]).isEqualTo(pojo.key)
    assertThat(result[Pojo::anotherKey.name]).isEqualTo(pojo.anotherKey)
  }

  @Test
  fun `should transform pojo with lists to map`() {

    data class Pojo(
      val key: String,
      val anotherKey: List<Int>
    )

    val pojo = Pojo(key = "value", anotherKey = listOf(4711, 4712))

    val result = serialize(pojo)

    assertThat(result[Pojo::key.name]).isEqualTo(pojo.key)
    assertThat(result[Pojo::anotherKey.name] as List<*>).containsOnlyElementsOf(pojo.anotherKey)
  }

  @Test
  fun `should transform complex pojo to map of maps`() {

    val pojo21 = Pojo2(keyZUZUZ = "pojo2", children = listOf(Pojo(key = "value1", anotherKey = listOf())))
    val pojo22 = Pojo2(keyZUZUZ = "pojo3", children = listOf(Pojo(key = "value2", anotherKey = listOf())))

    val pojo = Pojo(key = "value", anotherKey = listOf(pojo21, pojo22))

    val result = serialize(pojo)

    assertThat(result[Pojo::key.name]).isEqualTo(pojo.key)

    val children = result[Pojo::anotherKey.name] as List<Map<String, Any>>
    assertThat(children.size).isEqualTo(2)
    assertThat(children[0]).containsKey(Pojo2::keyZUZUZ.name)
    assertThat(children[0]).containsKey(Pojo2::children.name)
    assertThat(children[1]).containsKey(Pojo2::keyZUZUZ.name)
    assertThat(children[1]).containsKey(Pojo2::children.name)

    assertThat(children[0][Pojo2::keyZUZUZ.name]).isEqualTo(pojo21.keyZUZUZ)
    assertThat(children[1][Pojo2::keyZUZUZ.name]).isEqualTo(pojo22.keyZUZUZ)
  }

  @Test
  fun `should transform variable map with complex pojos to map of maps`() {
    val map = Variables
      .createVariables()
      .putValue("key", "value")
      .putValue("another-key", Pojo(key = "key", anotherKey = listOf(Pojo2(keyZUZUZ = "p2", children = listOf()))))

    val result = serialize(map)

    assertThat(result).containsKey("key")
    assertThat(result).containsKey("another-key")
    assertThat(result["key"]).isEqualTo(map["key"])

    val expectedPojoMap = result["another-key"] as Map<String, Any>
    assertThat(expectedPojoMap).containsKey(Pojo::key.name)
    assertThat(expectedPojoMap).containsKey(Pojo::anotherKey.name)

    assertThat(expectedPojoMap[Pojo::key.name]).isEqualTo("key")

    val elements = expectedPojoMap[Pojo::anotherKey.name] as List<Map<String, Any>>
    assertThat(elements).containsExactly(linkedMapOf(Pojo2::keyZUZUZ.name to "p2", Pojo2::children.name to listOf<String>()))
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

