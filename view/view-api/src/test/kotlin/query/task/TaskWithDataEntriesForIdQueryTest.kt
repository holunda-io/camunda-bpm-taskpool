package io.holunda.polyflow.view.query.task

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TaskWithDataEntriesForIdQueryTest {
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
      taskDefinitionKey = "task-def"
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
  fun `should filter by id`() {
    assertThat(TaskWithDataEntriesForIdQuery(id = "923847239").applyFilter(taskWithDataEntries)).isTrue
    assertThat(TaskWithDataEntriesForIdQuery(id = "other-id").applyFilter(taskWithDataEntries)).isFalse
  }
}
