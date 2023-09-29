package io.holunda.polyflow.view.query

import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.task.AllTasksQuery
import io.holunda.polyflow.view.query.task.TasksWithDataEntriesForGroupQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.declaredMemberProperties

internal class PageableSortableQueryTest {

  private val createTimeAsc = listOf("+createTime")
  private val nameDesc = listOf("-name")
  private val badField = listOf("+someTaskField")
  private val badOrdering = listOf("*createTime")
  private val user = User(username = "kermit", groups = setOf())

  @Test
  fun should_sanitize_task_query() {
    assertThat(AllTasksQuery(sort = createTimeAsc).apply { sanitizeSort(Task::class) }.sort).isEqualTo(createTimeAsc)
    assertThat(AllTasksQuery(sort = listOf()).apply { sanitizeSort(Task::class) }.sort).isNull()
    val badFieldException = assertThrows<IllegalArgumentException> { AllTasksQuery(sort = badField).apply { sanitizeSort(Task::class) } }
    assertThat(badFieldException.message).isEqualTo(
      "Sort parameter must be one of ${Task::class.declaredMemberProperties.joinToString(", ") { it.name }} but it was ${
        badField[0].substring(
          1
        )
      }."
    )

    val badOperatorException = assertThrows<IllegalArgumentException> { AllTasksQuery(sort = badOrdering).apply { sanitizeSort(Task::class) } }
    assertThat(badOperatorException.message).isEqualTo("Sort must start either with '+' or '-' but it was starting with '*'")
  }

  @Test
  fun should_sanitize_task_with_dataentry_query() {
    assertThat(TasksWithDataEntriesForGroupQuery(user = user, sort = createTimeAsc).apply { sanitizeSort(Task::class) }.sort).isEqualTo(createTimeAsc)
    assertThat(TasksWithDataEntriesForGroupQuery(user = user, sort = listOf()).apply { sanitizeSort(Task::class) }.sort).isNull()
    val badFieldException =
      assertThrows<IllegalArgumentException> { TasksWithDataEntriesForGroupQuery(user = user, sort = badField).apply { sanitizeSort(Task::class) } }
    assertThat(badFieldException.message).isEqualTo(
      "Sort parameter must be one of ${Task::class.declaredMemberProperties.joinToString(", ") { it.name }} but it was ${
        badField[0].substring(
          1
        )
      }."
    )

    val badOperatorException =
      assertThrows<IllegalArgumentException> { TasksWithDataEntriesForGroupQuery(user = user, sort = badOrdering).apply { sanitizeSort(Task::class) } }
    assertThat(badOperatorException.message).isEqualTo("Sort must start either with '+' or '-' but it was starting with '*'")
  }

  @Test
  fun should_sanitize_dataentry_query() {
    assertThat(DataEntriesQuery(sort = nameDesc).apply { sanitizeSort(DataEntry::class) }.sort).isEqualTo(nameDesc)
    assertThat(DataEntriesQuery(sort = listOf()).apply { sanitizeSort(DataEntry::class) }.sort).isNull()
    val badFieldException = assertThrows<IllegalArgumentException> { DataEntriesQuery(sort = badField).apply { sanitizeSort(DataEntry::class) } }
    assertThat(badFieldException.message).isEqualTo(
      "Sort parameter must be one of ${DataEntry::class.declaredMemberProperties.joinToString(", ") { it.name }} but it was ${
        badField[0].substring(
          1
        )
      }."
    )

    val badOperatorException = assertThrows<IllegalArgumentException> { DataEntriesQuery(sort = badOrdering).apply { sanitizeSort(DataEntry::class) } }
    assertThat(badOperatorException.message).isEqualTo("Sort must start either with '+' or '-' but it was starting with '*'")
  }
}
