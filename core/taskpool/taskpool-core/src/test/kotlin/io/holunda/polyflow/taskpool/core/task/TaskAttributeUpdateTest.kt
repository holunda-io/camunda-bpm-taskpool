package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.mapper.task.from
import io.holunda.camunda.taskpool.model.Task
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.test.aggregate.AggregateTestFixture
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Checks attribute engine commands fire correct events and change command model.
 */
class TaskAttributeUpdateTest {

  private val fixture: AggregateTestFixture<TaskAggregate> = AggregateTestFixture(TaskAggregate::class.java)
  private val processReference = ProcessReference(
    definitionKey = "process_key",
    instanceId = "0815",
    executionId = "12345",
    definitionId = "76543",
    name = "My process",
    applicationName = "myExample"
  )
  private val updatedProcessReference = ProcessReference(
    definitionKey = "process_key", // stays the same
    instanceId = "0815_new",
    executionId = "12345_new",
    definitionId = "76543_new",
    name = "My process 2",
    applicationName = "myExample" // stays the same
  )

  private lateinit var now: Date
  private lateinit var due: Date
  private lateinit var followUp: Date
  private lateinit var createdEvent: TaskCreatedEngineEvent
  private lateinit var updatedEvent: TaskAttributeUpdatedEngineEvent
  private lateinit var deletedEvent: TaskDeletedEngineEvent
  private lateinit var completedEvent: TaskCompletedEngineEvent
  private lateinit var updateCommand: UpdateAttributeTaskCommand

  @BeforeEach
  fun setUp() {
    now = Date()
    due = Date.from(Instant.now().plus(2, ChronoUnit.HOURS))
    followUp = Date.from(Instant.now().plus(4, ChronoUnit.HOURS))

    createdEvent = TaskCreatedEngineEvent(
      id = "4711",
      name = "Foo",
      createTime = now,
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      sourceReference = processReference,
      candidateUsers = setOf("kermit"),
      candidateGroups = setOf("muppetshow"),
      assignee = null,
      priority = 51,
      description = "Funky task",
      payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
      correlations = newCorrelations().addCorrelation("Request", "business123")
    )
    deletedEvent = TaskDeletedEngineEvent(
      id = "4711",
      name = "Foo",
      createTime = now,
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      sourceReference = processReference,
      deleteReason = "Test delete"
    )
    updatedEvent = TaskAttributeUpdatedEngineEvent(
      id = "4711",
      name = "New name",
      owner = "piggy",
      taskDefinitionKey = "new-foo",
      businessKey = "business456",
      sourceReference = updatedProcessReference,
      priority = 49,
      description = "Another task",
      formKey = "some",
      dueDate = due,
      payload = createdEvent.payload,
      correlations = createdEvent.correlations,
      followUpDate = followUp
    )
    completedEvent = TaskCompletedEngineEvent(
      id = "4711",
      name = "Foo",
      createTime = now,
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      sourceReference = processReference
    )

    updateCommand = UpdateAttributeTaskCommand(
      id = "4711",
      name = "New name",
      owner = "piggy",
      taskDefinitionKey = "new-foo",
      businessKey = "business456",
      sourceReference = updatedProcessReference,
      priority = 49,
      description = "Another task",
      dueDate = due,
      followUpDate = followUp,
      enriched = false // not enriched by default
    )
  }

  @Test
  fun `should event update of enriched command`() {
    fixture
      .given(createdEvent)
      .`when`(
        updateCommand.copy(
          enriched = true,
          payload = Variables.createVariables().putValueTyped("another-key", Variables.stringValue("another-value")),
          correlations = newCorrelations().addCorrelation("Request2", "business456")
        )
      ).expectEvents(
        updatedEvent.copy(
          payload = Variables.createVariables().putValueTyped("another-key", Variables.stringValue("another-value")),
          correlations = newCorrelations().addCorrelation("Request2", "business456")
        )
      )
  }

  @Test
  fun `should event update of non-enriched command`() {
    fixture
      .given(createdEvent)
      .`when`(
        updateCommand
      ).expectEvents(
        updatedEvent
      )
  }

  @Test
  fun `should deliver the modified state from aggregate after completion`() {
    val enrichedCommand = updateCommand.copy(
      enriched = true,
      payload = Variables.createVariables().putValueTyped("another-key", Variables.stringValue("another-value")),
      correlations = newCorrelations().addCorrelation("Request2", "business456")
    )
    fixture
      .given(createdEvent)
      .`when`(
        enrichedCommand
      ).expectState { taskAggregate ->
        assertThat(taskAggregate.task)
          .usingRecursiveComparison()
          .ignoringFields("candidateGroups", "candidateUsers", "createTime", "formKey")
          .isEqualTo(Task.from(enrichedCommand))

        assertThat(taskAggregate.task.candidateGroups).isEqualTo(createdEvent.candidateGroups)
        assertThat(taskAggregate.task.candidateUsers).isEqualTo(createdEvent.candidateUsers)
        assertThat(taskAggregate.task.createTime).isEqualTo(createdEvent.createTime)
        assertThat(taskAggregate.task.formKey).isEqualTo(createdEvent.formKey)
      }
  }


  @Test
  fun `should not event on completed task`() {
    fixture
      .given(createdEvent, completedEvent)
      .`when`(updateCommand)
      .expectNoEvents()
  }

  @Test
  fun `should not event on deleted task`() {
    fixture
      .given(createdEvent, deletedEvent)
      .`when`(updateCommand)
      .expectNoEvents()
  }

}
