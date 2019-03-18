package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.view.FormUrlResolver
import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mapstruct.factory.Mappers
import org.springframework.test.util.ReflectionTestUtils

class TaskWithDataEntriesMapperTest {

  private val formUrlResolver = object : FormUrlResolver {
    override fun resolveUrl(task: Task) =
      "http://localhost:8080/test/forms/the-task/id/1"
    override fun resolveUrl(processDefinition: ProcessDefinition) = "http://localhost:8080/test/start"
  }

  private val sourceReference: ProcessReference = ProcessReference(
    applicationName = "test",
    name = "Test Process",
    instanceId = "1",
    executionId = "1",
    definitionKey = "test-process",
    definitionId = "test-process#1"
  )

  private val mapper = Mappers.getMapper(TaskWithDataEntriesMapper::class.java)!!

  @Before
  fun setUp() {
    ReflectionTestUtils.setField(mapper, "formUrlResolver", formUrlResolver)
  }

  @Test
  fun `resolves url for given task`() {
    val task = Task(
      id = "1",
      sourceReference = sourceReference,
      taskDefinitionKey = "the-task",
      formKey = "the-task"
    )

    val dto = mapper.dto(task)

    assertThat(dto.url).isEqualTo("http://localhost:8080/test/forms/the-task/id/1")
    assertThat(dto.formKey).isEqualTo("the-task")
    assertThat(dto.processName).isEqualTo(sourceReference.name)

  }
}
