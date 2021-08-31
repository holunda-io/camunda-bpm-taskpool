package io.holunda.polyflow.view.query.task

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.auth.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TaskWithDataEntriesForUserQueryTest {

  private val taskWithDataEntries = TaskWithDataEntries(
    task = Task(
      id = "923847239",
      sourceReference = ProcessReference(
        instanceId = "instance-id",
        executionId = "exec-id",
        definitionId = "def-id",
        definitionKey = "def-key",
        name = "process name",
        applicationName = "test-application"
      ),
      taskDefinitionKey = "task-def",
      candidateUsers = setOf("kermit"),
      assignee = "piggy",
      candidateGroups = setOf("muppets"),
    ), dataEntries = listOf(
      DataEntry(
        entryType = "io.type",
        entryId = "0239480234",
        type = "data entry",
        applicationName = "test-application",
        name = "Data Entry for case 4711",
      )
    )
  )

  @Test
  fun `should filter by user`() {
    assertThat(TasksWithDataEntriesForUserQuery(user = User(username = "kermit", groups = setOf("muppets"))).applyFilter(taskWithDataEntries)).isTrue
    assertThat(TasksWithDataEntriesForUserQuery(user = User(username = "kermit", groups = setOf())).applyFilter(taskWithDataEntries)).isTrue
    assertThat(TasksWithDataEntriesForUserQuery(user = User(username = "piggy", groups = setOf())).applyFilter(taskWithDataEntries)).isTrue
    assertThat(TasksWithDataEntriesForUserQuery(user = User(username = "ironman", groups = setOf("muppets"))).applyFilter(taskWithDataEntries)).isTrue
    assertThat(TasksWithDataEntriesForUserQuery(user = User(username = "ironman", groups = setOf("avengers"))).applyFilter(taskWithDataEntries)).isFalse
  }
}
