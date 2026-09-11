package io.holunda.polyflow.view.query.task

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
internal class TaskQueriesDeserializationTest {

  private val objectMapper: ObjectMapper = jacksonObjectMapper().configurePolyflowJacksonObjectMapper()

  companion object {
    @JvmStatic
    fun provideTaskQueries() = Stream.of(
      Arguments.of(
        AllTasksQuery::class.java,
        AllTasksQuery(
          page = 1,
          size = 50,
          sort = listOf("+name"),
          filters = listOf("task.name=test")
        )
      ),
      Arguments.of(
        AllTasksWithDataEntriesQuery::class.java,
        AllTasksWithDataEntriesQuery(
          page = 1,
          size = 50,
          sort = listOf("+name"),
          filters = listOf("task.name=test")
        )
      ),
      Arguments.of(
        TaskCountByApplicationQuery::class.java,
        TaskCountByApplicationQuery()
      ),
      Arguments.of(
        TaskForIdQuery::class.java,
        TaskForIdQuery(id = "4712")
      ),
      Arguments.of(
        TasksForApplicationQuery::class.java,
        TasksForApplicationQuery(applicationName = "appl")
      ),
      Arguments.of(
        TasksForGroupQuery::class.java,
        TasksForGroupQuery(
          user = User(username = "kermit", groups = setOf("muppets")),
          page = 1,
          size = 50,
          sort = listOf("+name"),
          filters = listOf("task.name=test")
        )
      ),
      Arguments.of(
        TasksForUserQuery::class.java,
        TasksForUserQuery(
          user = User(username = "kermit", groups = setOf("muppets")),
          page = 1,
          size = 50,
          sort = listOf("+name"),
          filters = listOf("task.name=test")
        )
      ),
      Arguments.of(
        TasksWithDataEntriesForGroupQuery::class.java,
        TasksWithDataEntriesForGroupQuery(
          user = User(username = "kermit", groups = setOf("muppets")),
          page = 1,
          size = 50,
          sort = listOf("+name"),
          filters = listOf("task.name=test")
        )
      ),
      Arguments.of(
        TasksWithDataEntriesForUserQuery::class.java,
        TasksWithDataEntriesForUserQuery(
          user = User(username = "kermit", groups = setOf("muppets")),
          page = 1,
          size = 50,
          sort = listOf("+name"),
          filters = listOf("task.name=test")
        )
      ),
      Arguments.of(
        TaskWithDataEntriesForIdQuery::class.java,
        TaskWithDataEntriesForIdQuery(
          id = "4713"
        )
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("provideTaskQueries")
  fun <Q : Any> `checks serialization is not changing the query`(type: Class<Q>, query: Q) {
    val serialized = objectMapper.writeValueAsString(query)
    val deserialized: Q = objectMapper.readValue(serialized, type)
    assertThat(deserialized).isEqualTo(query)
  }
}
