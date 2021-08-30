package io.holunda.polyflow.example.tasklist.rest.mapper

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.FormUrlResolver
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mapstruct.factory.Mappers
import org.springframework.test.util.ReflectionTestUtils

class TaskWithDataEntriesMapperTest {

  private val formUrlResolver = object : FormUrlResolver {
    override fun resolveUrl(dataEntry: DataEntry) = "http://localhost:8080/test/forms/bo/id/1"
    override fun resolveUrl(task: Task) = "http://localhost:8080/test/forms/the-task/id/1"
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

  @Test
  fun `resolves url for given bo`() {
    val bo = DataEntry(
      entryId = "1",
      entryType = "bo",
      applicationName = "my-app",
      formKey = "the-bo",
      type = "BO",
      name = "BO 1"
    )

    val dto = mapper.dto(bo)

    assertThat(dto.url).isEqualTo("http://localhost:8080/test/forms/bo/id/1")
    assertThat(dto.formKey).isEqualTo("the-bo")
    assertThat(dto.applicationName).isEqualTo("my-app")

  }

}
