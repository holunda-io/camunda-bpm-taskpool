package io.holunda.polyflow.view.query.task

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AllTasksQueryTest {
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
    name = "task 1",
    description = "description",
    taskDefinitionKey = "task-def",
    candidateUsers = setOf("kermit"),
    assignee = "piggy",
    candidateGroups = setOf("muppets"),
  )

  @Test
  fun `should always return true`() {
    assertThat(AllTasksQuery().applyFilter(task)).isTrue
  }
}
