package io.holunda.polyflow.view.query.process

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import io.holunda.polyflow.view.auth.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * Test to make sure all queries and responses are deserializable.
 */
internal class ProcessDefinitionQueriesDeserializationTest {

  private val objectMapper: ObjectMapper = jacksonObjectMapper().configurePolyflowJacksonObjectMapper()

  companion object {
    @JvmStatic
    fun provideProcessDefinitionQueries(): Stream<Arguments> = Stream.of(
      Arguments.of(
        ProcessDefinitionsStartableByUserQuery::class.java,
        ProcessDefinitionsStartableByUserQuery(
          user = User(username = "kermit", groups = setOf("muppets"))
        )
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("provideProcessDefinitionQueries")
  fun <Q : Any> `checks serialization is not changing the query`(type: Class<Q>, query: Q) {
    val serialized = objectMapper.writeValueAsString(query)
    val deserialized: Q = objectMapper.readValue(serialized, type)
    assertThat(deserialized).isEqualTo(deserialized)
  }
}
