package rest.mapper

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.example.tasklist.TasklistConfiguration
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.ApplicationUrlLookup
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.TaskUrlResolver
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.TaskWithDataEntriesMapper
import io.holunda.camunda.taskpool.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mapstruct.factory.Mappers
import org.springframework.test.util.ReflectionTestUtils

class TaskWithDataEntriesMapperTest {

  private val taskUrlResolver: TaskUrlResolver = with(TasklistConfiguration()) {
    defaultTaskUrlResolver(defaultApplicationUrlLookup())
  }

  private val sourceReference: ProcessReference = ProcessReference(
    applicationName = "test",
    processName = "Test Process",
    instanceId = "1",
    executionId = "1",
    definitionKey = "test-process",
    definitionId = "test-process#1"
  )

  private val mapper = Mappers.getMapper(TaskWithDataEntriesMapper::class.java)!!

  @Before
  fun setUp() {
    ReflectionTestUtils.setField(mapper, "taskUrlResolver", taskUrlResolver)
  }

  @Test
  fun `resolves url for given task`() {
    val task = Task(
      id="1",
      sourceReference = sourceReference,
      taskDefinitionKey = "the-task",
      formKey = "forms/the-task.html"
    )

    val dto = mapper.dto(task)

    assertThat(dto.url).isEqualTo("http://localhost:8080/test/forms/the-task.html?taskId=1")
    assertThat(dto.formKey).isEqualTo("forms/the-task.html")
    assertThat(dto.processName).isEqualTo(sourceReference.processName)

  }
}
