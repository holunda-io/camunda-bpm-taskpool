package io.holunda.polyflow.view.query.task

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TasksForUserQueryTest {
  private val task = Task(
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
  )

  @Test
  fun `should filter by user`() {
    assertThat(TasksForUserQuery(assignedToMeOnly = false, user = User(username = "kermit", groups = setOf("muppets"))).applyFilter(task)).isTrue
    assertThat(TasksForUserQuery(assignedToMeOnly = false, user = User(username = "kermit", groups = setOf())).applyFilter(task)).isTrue
    assertThat(TasksForUserQuery(assignedToMeOnly = false, user = User(username = "piggy", groups = setOf())).applyFilter(task)).isTrue
    assertThat(TasksForUserQuery(assignedToMeOnly = false, user = User(username = "ironman", groups = setOf("muppets"))).applyFilter(task)).isTrue
    assertThat(TasksForUserQuery(assignedToMeOnly = false, user = User(username = "ironman", groups = setOf("avengers"))).applyFilter(task)).isFalse
  }
}
