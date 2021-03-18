package io.holunda.camunda.taskpool.cockpit.rest.mapper

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.camunda.taskpool.cockpit.service.TaskEventWithMetaData
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.MetaData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class TaskEventMapperTest {

  private lateinit var taskEventMapper: TaskEventMapper

  @BeforeEach
  fun init() {
    taskEventMapper = TaskEventMapperImpl()
  }

  @Test
  fun `should map the instant`() {
    val now = Instant.now()
    val event = TaskCreatedEngineEvent(
      id = "4711",
      sourceReference = ProcessReference("i1", "e1", "d1", "d", "n", "a", null),
      taskDefinitionKey = "")

    val dto = taskEventMapper.dto(TaskEventWithMetaData(event, now, MetaData.emptyInstance()))
    assertThat(dto.created.toInstant()).isEqualTo(now)
  }
}
