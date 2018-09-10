package io.holunda.camunda.taskpool.core

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.TaskCreatedEvent
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.*

class TaskAggregateTest {

  private val fixture: AggregateTestFixture<TaskAggregate> = AggregateTestFixture<TaskAggregate>(TaskAggregate::class.java)
  private lateinit var now: Date

  @Before
  fun setUp() {
    now = Date()
  }

  @Test
  fun `should create task`() {
    fixture
      .givenNoPriorActivity()
      .`when`(
        CreateTaskCommand(
          id = "4711",
          name = "Foo",
          createTime = now,
          eventName = "create",
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          enriched = true,
          processReference = ProcessReference(
            processDefinitionKey = "process_key",
            processInstanceId = "0815",
            executionId = "12345",
            processDefinitionId = "76543"
          ),
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = mutableMapOf<String, Any>( "key" to "value")))
      .expectEvents(
        TaskCreatedEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          processReference = ProcessReference(
            processDefinitionKey = "process_key",
            processInstanceId = "0815",
            executionId = "12345",
            processDefinitionId = "76543"
          ),
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = mutableMapOf<String, Any>( "key" to "value")
      ))
  }
}
