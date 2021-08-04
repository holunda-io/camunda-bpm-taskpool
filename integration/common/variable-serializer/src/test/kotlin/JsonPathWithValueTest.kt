package io.holunda.polyflow.variable.serializer

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.variable.serializer.toJsonPathsWithValues
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

internal class JsonPathWithValueTest {

  private val now = Date.from(Instant.now())
  private val mapper = jacksonObjectMapper()

  @Before
  fun `setup jackson`() {
    mapper.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    mapper.registerModule(JavaTimeModule())
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
  }

  @Test
  fun `should convert map of depth 1 with primitives`() {
    val payload = createVariables()
      .putValue("key-string", "value")
      .putValue("key-long", 19L)
      .putValue("key-date", now)
      .putValue("key-int", 56)
      .putValue("key-bool", true)
      .putValue("key-float", 101.1F)

    val result = payload.toJsonPathsWithValues(limit = -1)

    assertThat(result.keys).containsExactlyInAnyOrderElementsOf(payload.keys)
    payload.entries.forEach {
      assertThat(result).containsKey(it.key)
      assertThat(result[it.key]).isEqualTo(it.value)
    }
  }

  @Test
  fun `should convert a deep map with primitives`() {
    val flat = mapOf(
      "key-string" to "value",
      "key-long" to 19L,
      "key-date" to now,
      "key-int" to 56,
      "key-bool" to true
    )
    val deep = createVariables().apply {
      putAll(flat)
      putValue(
        "key-map", mapOf(
          "child1" to "string",
          "child2" to mapOf(
            "grand-child1" to "grand-child-value",
            "grand-child2" to 451.01F
          )
        )
      )
    }

    val result = deep.toJsonPathsWithValues(limit = -1)

    assertThat(result.keys).containsExactlyInAnyOrderElementsOf(
      flat.keys.plus(
        listOf(
          "key-map.child1",
          "key-map.child2.grand-child1",
          "key-map.child2.grand-child2"
        )
      )
    )
    flat.entries.forEach {
      assertThat(result).containsKey(it.key)
      assertThat(result[it.key]).isEqualTo(it.value)
    }

    assertThat(result["key-map.child1"]).isEqualTo("string")
    assertThat(result["key-map.child2.grand-child1"]).isEqualTo("grand-child-value")
    assertThat(result["key-map.child2.grand-child2"]).isEqualTo(451.01F)
  }

  @Test
  fun `should convert a deep map with primitives limited by level`() {
    val flat = mapOf(
      "key-string" to "value",
      "key-long" to 19L,
      "key-date" to now,
      "key-int" to 56,
      "key-bool" to true
    )
    val deep = createVariables().apply {
      putAll(flat)
      putValue(
        "key-map", mapOf(
          "child1" to "string",
          "child2" to mapOf(
            "grand-child1" to "grand-child-value",
            "grand-child2" to 451.01F
          )
        )
      )
    }

    val result = deep.toJsonPathsWithValues(limit = 1)

    assertThat(result.keys).containsExactlyInAnyOrderElementsOf(
      flat.keys.plus(
        listOf(
          "key-map.child1"
        )
      )
    )
    flat.entries.forEach {
      assertThat(result).containsKey(it.key)
      assertThat(result[it.key]).isEqualTo(it.value)
    }
    assertThat(result["key-map.child1"]).isEqualTo("string")
    assertThat(result["key-map.child2"]).isNull()
  }

  @Test
  fun `should ignore complex object`() {
    val payload = createVariables().apply {
      put("key", Pojo1("value", listOf(Pojo2("value", listOf()))))
    }
    assertThat(payload.toJsonPathsWithValues()).isEmpty()
  }

}
