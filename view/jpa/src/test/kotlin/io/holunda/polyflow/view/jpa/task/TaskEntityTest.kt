package io.holunda.polyflow.view.jpa.task

import io.holunda.polyflow.view.jpa.emptyTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

internal class TaskEntityTest {

  @Test
  fun `test toString`() {
    val now = Instant.now()
    val task = emptyTask().apply {
      taskId = "id-123"
      taskDefinitionKey = "def-098"
      name = "name"
      createdDate = now
    }
    assertThat(task.toString()).isEqualTo("Task[taskId=id-123, taskDefinitionKey=def-098, name=name, created=$now]")
  }
}
