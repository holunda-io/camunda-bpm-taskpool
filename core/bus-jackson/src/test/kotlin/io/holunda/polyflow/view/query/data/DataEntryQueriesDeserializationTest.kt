package io.holunda.polyflow.view.query.data

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
internal class DataEntryQueriesDeserializationTest {

  private val objectMapper: ObjectMapper = jacksonObjectMapper().configurePolyflowJacksonObjectMapper()

  companion object {
    @JvmStatic
    fun provideDataEntryQueries(): Stream<Arguments> = Stream.of(
      Arguments.of(
        DataEntryForIdentityQuery::class.java,
        DataEntryForIdentityQuery(entryId = "4711", entryType = "domain.type")
      ), Arguments.of(
        DataEntryForIdentityQuery::class.java,
        DataEntryForIdentityQuery(QueryDataIdentity(entryId = "4711", entryType = "domain.type"))
      ),

      Arguments.of(
        DataEntriesForUserQuery::class.java,
        DataEntriesForUserQuery(
          user = User(
            username = "kermit", groups = setOf("muppets")
          ), page = 1, size = 50, sort = listOf("+name"), filters = listOf("data.name=test")
        )
      ), Arguments.of(
        DataEntriesForDataEntryTypeQuery::class.java,
        DataEntriesForDataEntryTypeQuery(
          entryType = "domain.type", page = 1, size = 50, sort = listOf("+name")
        )
      )
    )
  }

  @ParameterizedTest
  @MethodSource("provideDataEntryQueries")
  fun <Q : Any> `checks serialization is not changing the query`(type: Class<Q>, query: Q) {
    val serialized = objectMapper.writeValueAsString(query)
    val deserialized: Q = objectMapper.readValue(serialized, type)
    assertThat(deserialized).isEqualTo(deserialized)
  }
}
