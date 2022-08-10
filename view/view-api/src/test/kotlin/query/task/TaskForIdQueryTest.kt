package io.holunda.polyflow.view.query.task

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TaskForIdQueryTest {
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
    taskDefinitionKey = "task-def"
  )

  @Test
  fun `should filter by id`() {
    assertThat(TaskForIdQuery(id = "923847239").applyFilter(task)).isTrue
    assertThat(TaskForIdQuery(id = "other-id").applyFilter(task)).isFalse
  }
}
