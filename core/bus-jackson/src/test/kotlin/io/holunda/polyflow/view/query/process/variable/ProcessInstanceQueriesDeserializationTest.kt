package io.holunda.polyflow.view.query.process.variable

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import io.holunda.polyflow.view.query.process.variable.filter.ProcessVariableFilterExactlyOne
import io.holunda.polyflow.view.query.process.variable.filter.ProcessVariableFilterOneOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * Test to make sure all queries and responses are deserializable.
 */
internal class ProcessInstanceQueriesDeserializationTest {

  private val objectMapper: ObjectMapper = jacksonObjectMapper().configurePolyflowJacksonObjectMapper()

  companion object {
    @JvmStatic
    fun provideProcessVariableQueries(): Stream<Arguments> = Stream.of(
      Arguments.of(
        ProcessVariablesForInstanceQuery::class.java,
        ProcessVariablesForInstanceQuery(
          processInstanceId = "4712",
          variableFilter = listOf(
            ProcessVariableFilterOneOf(setOf("var1", "var2")),
            ProcessVariableFilterExactlyOne("var3")
          )
        )
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("provideProcessVariableQueries")
  fun <Q : Any> `checks serialization is not changing the query`(type: Class<Q>, query: Q) {
    val serialized = objectMapper.writeValueAsString(query)
    val deserialized: Q = objectMapper.readValue(serialized, type)
    assertThat(deserialized).isEqualTo(deserialized)
  }
}
