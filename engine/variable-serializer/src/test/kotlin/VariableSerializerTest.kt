import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.variable.serializer.serialize
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SimpleDataEntryCommandSenderTest {

  private val mapper = jacksonObjectMapper()

  @Test
  fun `should return the variables map`() {

    val map = Variables
      .createVariables()
      .putValue("key", "value")
      .putValue("another-key", 4711)

    val result = serialize(map, mapper)

    assertThat(result).isEqualTo(map)
    map.forEach {
      assertThat(result).containsKey(it.key)
      assertThat(result[it.key]).isEqualTo(it.value)
    }
  }

  @Test
  fun `should transform simple pojo to map`() {

    val pojo = Pojo3(key = "value", anotherKey = 4711)

    val result = serialize(pojo, mapper)

    assertThat(result[Pojo3::key.name]).isEqualTo(pojo.key)
    assertThat(result[Pojo3::anotherKey.name]).isEqualTo(pojo.anotherKey)
  }

  @Test
  fun `should transform pojo with instant to map`() {

    mapper.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    mapper.registerModule(JavaTimeModule())
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)

    val now = Instant.parse("2020-10-15T07:20:05.871641Z")
    val pojo = Pojo5(key = "value", ts = now, date = now.atOffset(ZoneOffset.UTC))
    val result = serialize(pojo, mapper)

    assertThat(result[Pojo5::key.name]).isEqualTo(pojo.key)
    assertThat(result[Pojo5::date.name]).isEqualTo("${pojo.date}") // dates to strings
    assertThat(result[Pojo5::ts.name]).isEqualTo("${pojo.ts}") // instant to string
  }


  @Test
  fun `should transform pojo with lists to map`() {

    val pojo = Pojo4(key = "value", anotherKey = listOf(4711, 4712))

    val result = serialize(pojo, mapper)

    assertThat(result[Pojo4::key.name]).isEqualTo(pojo.key)
    assertThat(result[Pojo4::anotherKey.name] as List<*>).containsExactlyElementsOf(pojo.anotherKey)
  }

  @Test
  fun `should transform complex pojo to map of maps`() {

    val pojo21 = Pojo2(keyZUZUZ = "pojo2", children = listOf(Pojo1(key = "value1", anotherKey = listOf())))
    val pojo22 = Pojo2(keyZUZUZ = "pojo3", children = listOf(Pojo1(key = "value2", anotherKey = listOf())))

    val pojo = Pojo1(key = "value", anotherKey = listOf(pojo21, pojo22))

    val result = serialize(pojo, mapper)

    assertThat(result[Pojo1::key.name]).isEqualTo(pojo.key)

    @Suppress("UNCHECKED_CAST")
    val children = result[Pojo1::anotherKey.name] as List<Map<String, Any>>
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
      .putValue("another-key", Pojo1(key = "key", anotherKey = listOf(Pojo2(keyZUZUZ = "p2", children = listOf()))))

    val result = serialize(map, mapper)

    assertThat(result).containsKey("key")
    assertThat(result).containsKey("another-key")
    assertThat(result["key"]).isEqualTo(map["key"])

    @Suppress("UNCHECKED_CAST")
    val expectedPojoMap = result["another-key"] as Map<String, Any>
    assertThat(expectedPojoMap).containsKey(Pojo1::key.name)
    assertThat(expectedPojoMap).containsKey(Pojo1::anotherKey.name)

    assertThat(expectedPojoMap[Pojo1::key.name]).isEqualTo("key")

    @Suppress("UNCHECKED_CAST")
    val elements = expectedPojoMap[Pojo1::anotherKey.name] as List<Map<String, Any>>
    assertThat(elements).containsExactly(linkedMapOf(Pojo2::keyZUZUZ.name to "p2", Pojo2::children.name to listOf<String>()))
  }
}

data class Pojo1(
  val key: String,
  val anotherKey: List<Pojo2>
)

data class Pojo2(
  val keyZUZUZ: String,
  var children: List<Pojo1> = listOf()
)

data class Pojo3(
  val key: String,
  val anotherKey: Int
)

data class Pojo4(
  val key: String,
  val anotherKey: List<Int>
)

data class Pojo5(
  val key: String,
  val ts: Instant,
  val date: OffsetDateTime
)
