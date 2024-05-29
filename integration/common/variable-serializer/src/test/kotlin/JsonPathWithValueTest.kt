package io.holunda.polyflow.variable.serializer

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.variable.serializer.EqualityPathFilter.Companion.all
import io.holunda.camunda.variable.serializer.EqualityPathFilter.Companion.eqExclude
import io.holunda.camunda.variable.serializer.EqualityPathFilter.Companion.eqInclude
import io.holunda.camunda.variable.serializer.EqualityPathFilter.Companion.none
import io.holunda.camunda.variable.serializer.toJsonPathsWithValues
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

internal class JsonPathWithValueTest {

  private val now = Date.from(Instant.now())
  private val mapper = jacksonObjectMapper()

  @BeforeEach
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

    assertThat(result.keys()).containsExactlyInAnyOrderElementsOf(payload.keys)
    payload.entries.forEach {
      assertThat(result.keys()).contains(it.key)
      assertThat(result.first { entry -> entry.first == it.key }.second).isEqualTo(it.value)
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

    assertThat(result.keys()).containsExactlyInAnyOrderElementsOf(
      flat.keys.plus(
        listOf(
          "key-map.child1",
          "key-map.child2.grand-child1",
          "key-map.child2.grand-child2"
        )
      )
    )
    flat.entries.forEach {
      assertThat(result.keys()).contains(it.key)
      assertThat(result.first { entry -> entry.first == it.key }.second).isEqualTo(it.value)
    }

    assertThat(result).contains("key-map.child1" to "string")
    assertThat(result).contains("key-map.child2.grand-child1" to "grand-child-value")
    assertThat(result).contains("key-map.child2.grand-child2" to 451.01F)
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

    assertThat(result.keys()).containsExactlyInAnyOrderElementsOf(
      flat.keys.plus(
        listOf(
          "key-map.child1"
        )
      )
    )
    flat.entries.forEach {
      assertThat(result.keys()).contains(it.key)
      assertThat(result).contains(it.key to it.value)
    }
    assertThat(result).contains("key-map.child1" to "string")
    assertThat(result.keys()).doesNotContain("key-map.child2")
  }

  @Test
  fun `should ignore complex object`() {
    val payload = createVariables().apply {
      put("key", Pojo1("value", listOf(Pojo2("value", listOf()))))
    }
    assertThat(payload.toJsonPathsWithValues()).isEmpty()
  }


  @Test
  fun `should ignore attribute by name`() {
    val payload = createVariables().apply {
      put("key", "value")
      put("to-ignore", "should not be there")
    }
    assertThat(payload.toJsonPathsWithValues(filters = listOf(eqExclude("to-ignore"))).keys()).containsOnly("key")

  }

  @Test
  fun `should include attribute by name`() {
    val payload = createVariables().apply {
      put("key", "value")
      put("to-ignore", "should not be there")
    }
    assertThat(payload.toJsonPathsWithValues(filters = listOf(eqInclude("key"))).keys()).containsOnly("key")
  }

  @Test
  fun `should include and exclude attribute by name`() {
    val payload = createVariables().apply {
      put("include1", "value")
      put("include2", "value")
      put("to-ignore", "should not be there")
    }
    assertThat(
      payload.toJsonPathsWithValues(
        filters = listOf(
          eqInclude("include1"),
          eqInclude("include2"),
          eqExclude("to-ignore")
        )
      ).keys()
    ).containsOnly("include1", "include2")
  }

  @Test
  fun `should include nested attributes by name`() {
    val payload = createVariables().apply {
      put("include1", mapOf("ignore" to "should not be there", "key" to "value"))
      put("include2", "value")
    }
    assertThat(
      payload.toJsonPathsWithValues(
        filters = listOf(
          eqInclude("include1.key"),
          eqInclude("include2"),
        )
      ).keys()
    ).containsOnly("include1.key", "include2")
  }


  @Test
  fun `should accept all attributes`() {
    val payload = createVariables().apply {
      put("key", "value")
      put("other", "value2")
    }
    assertThat(payload.toJsonPathsWithValues(filters = listOf(all())).keys()).containsOnly("key", "other")
  }

  @Test
  fun `should ignore all attributes`() {
    val payload = createVariables().apply {
      put("key", "value")
      put("other", "value2")
    }
    assertThat(payload.toJsonPathsWithValues(filters = listOf(none()))).isEmpty()
  }

  @Test
  fun `should map list to multiple pairs`() {
    val payload = createVariables().apply {
      put("multiple", listOf("value-1", "value-2"))
    }

    val result = payload.toJsonPathsWithValues()

    assertThat(result).hasSize(2)
    assertThat(result).contains("multiple" to "value-1")
    assertThat(result).contains("multiple" to "value-2")
  }

  @Test
  fun `should map list of lists`() {
    val payload = createVariables().apply {
      put("multiple", listOf(listOf("value-1", "value-2"), listOf("value-3", "value-4")))
    }
      val result = payload.toJsonPathsWithValues()

      assertThat(result).hasSize(4)
      assertThat(result).contains("multiple" to "value-1")
      assertThat(result).contains("multiple" to "value-2")
      assertThat(result).contains("multiple" to "value-3")
      assertThat(result).contains("multiple" to "value-4")
    }

  @Test
  fun `should map list of maps`() {
    val payload = createVariables().apply {
      put("multiple", listOf(mapOf("deepKey1" to "value-1"), mapOf("deepKey2" to "value-2")))
    }
    val result = payload.toJsonPathsWithValues()

    assertThat(result).hasSize(2)
    assertThat(result).contains("multiple.deepKey1" to "value-1")
    assertThat(result).contains("multiple.deepKey2" to "value-2")
  }
}

internal fun  Set<Pair<String, Any>>.keys(): List<String> {
  return this.map { it.first }
}
