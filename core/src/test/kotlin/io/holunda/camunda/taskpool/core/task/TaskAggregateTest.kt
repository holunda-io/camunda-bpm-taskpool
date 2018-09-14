package io.holunda.camunda.taskpool.core

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.core.task.TaskAggregate
import org.axonframework.test.aggregate.AggregateTestFixture
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.junit.Before
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
          payload = Variables.createVariables().putValueTyped("key", stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ))
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
          payload = Variables.createVariables().putValueTyped("key", stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ))
  }

  @Test
  fun `should complete task`() {
    fixture
      .given(
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
          payload = Variables.createVariables().putValueTyped("key", stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ))
      .`when`(
        CompleteTaskCommand(
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
          caseReference = null,
          dueDate = null,
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("another", stringValue("some")),
          correlations = newCorrelations().addCorrelation("Request", "business456")
        ))
      .expectEvents(
        TaskCompletedEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
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
          caseReference = null,
          dueDate = null,
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("another", stringValue("some")),
          correlations = newCorrelations().addCorrelation("Request", "business456")
        )
      )
  }

  @Test
  fun`should not complete deleted task`() {
    fixture
      .given(
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
          payload = Variables.createVariables().putValueTyped("key", stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ),
        TaskDeletedEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
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
          caseReference = null,
          dueDate = null,
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("another", stringValue("some1")),
          correlations = newCorrelations().addCorrelation("Request", "business789"),
          deleteReason = "Deleted, because not needed"
        ))
      .`when`(
        CompleteTaskCommand(
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
          caseReference = null,
          dueDate = null,
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("another", stringValue("some")),
          correlations = newCorrelations().addCorrelation("Request", "business456")
        )
      ).expectNoEvents()
  }

  @Test
  fun`should not delete deleted task`() {
    fixture
      .given(
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
          payload = Variables.createVariables().putValueTyped("key", stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ),
        TaskDeletedEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
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
          caseReference = null,
          dueDate = null,
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("another", stringValue("some1")),
          correlations = newCorrelations().addCorrelation("Request", "business789"),
          deleteReason = "Deleted, because not needed"
        ))
      .`when`(
        DeleteTaskCommand(
          id = "4711",
          taskDefinitionKey = "foo",
          deleteReason = "Not possible"
        )
      ).expectNoEvents()
  }

  @Test
  fun `should delete task`() {
    fixture
      .given(
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
          payload = Variables.createVariables().putValueTyped("key", stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ))
      .`when`(
        DeleteTaskCommand(
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
          caseReference = null,
          dueDate = null,
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("another", stringValue("some1")),
          correlations = newCorrelations().addCorrelation("Request", "business789"),
          deleteReason = "Deleted, because not needed"
        ))
      .expectEvents(
        TaskDeletedEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
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
          caseReference = null,
          dueDate = null,
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          assignee = "kermit",
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("another", stringValue("some1")),
          correlations = newCorrelations().addCorrelation("Request", "business789"),
          deleteReason = "Deleted, because not needed"
        )
      )
  }
}
