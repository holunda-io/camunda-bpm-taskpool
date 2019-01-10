package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import org.axonframework.test.aggregate.AggregateTestFixture
import org.camunda.bpm.engine.variable.Variables
import org.junit.Before
import org.junit.Test
import java.util.*

class TaskMarkToBeCompletedTest {

  private val fixture: AggregateTestFixture<TaskAggregate> = AggregateTestFixture<TaskAggregate>(TaskAggregate::class.java)
  private lateinit var now: Date
  private lateinit var assigned: TaskCreatedEngineEvent
  private lateinit var unassigned: TaskCreatedEngineEvent
  private lateinit var deleted: TaskDeletedEngineEvent
  private lateinit var completed: TaskCompletedEngineEvent

  private val processReference = ProcessReference(
    definitionKey = "process_key",
    instanceId = "0815",
    executionId = "12345",
    definitionId = "76543",
    name = "My process",
    applicationName = "myExample"
  )

  @Before
  fun setUp() {
    now = Date()
    assigned = TaskCreatedEngineEvent(
      id = "4711",
      name = "Foo",
      createTime = now,
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      sourceReference = processReference,
      assignee = "kermit",
      candidateUsers = setOf("kermit", "gonzo"),
      candidateGroups = setOf("muppets"),
      priority = 51,
      description = "Funky task",
      payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
      correlations = newCorrelations().addCorrelation("Request", "business123")
    )

    unassigned = TaskCreatedEngineEvent(
      id = "4711",
      name = "Foo",
      createTime = now,
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      sourceReference = processReference,
      assignee = null,
      candidateUsers = setOf("kermit", "gonzo"),
      candidateGroups = setOf("muppets"),
      priority = 51,
      description = "Funky task",
      payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
      correlations = newCorrelations().addCorrelation("Request", "business123")
    )

    deleted = TaskDeletedEngineEvent(
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

    completed = TaskCompletedEngineEvent(
      id = "4711",
      name = "Foo",
      createTime = now,
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      sourceReference = processReference
    )
  }


  @Test
  fun `should re-assign assigned task and mark task to be completed`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(assigned)
      .`when`(
        CompleteInteractionTaskCommand(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          assignee = "gonzo"
        )
      ).expectEvents(
        TaskUnclaimedEvent(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          formKey = "some"
        ),
        TaskClaimedEvent(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          assignee = "gonzo",
          formKey = "some"
        ),
        TaskToBeCompletedEvent(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          formKey = "some"
        )
      )
  }

  @Test
  fun `should mark task to be completed if no assignee`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(assigned)
      .`when`(
        CompleteInteractionTaskCommand(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          assignee = null
        )
      ).expectEvents(
        TaskToBeCompletedEvent(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          formKey = "some"
        )
      )
  }

  @Test
  fun `should claim and mark task to be completed`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(unassigned)
      .`when`(
        CompleteInteractionTaskCommand(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          assignee = "gonzo"
        )
      ).expectEvents(
        TaskClaimedEvent(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          assignee = "gonzo",
          formKey = "some"
        ),
        TaskToBeCompletedEvent(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          formKey = "some"
        )
      )
  }

  @Test
  fun `should not re-assign but mark task to be completed if the same assignee`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(assigned)
      .`when`(
        CompleteInteractionTaskCommand(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          assignee = "kermit"
        )
      ).expectEvents(
        TaskToBeCompletedEvent(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          formKey = "some"
        )
      )
  }

  @Test
  fun `should not claim but mark task to be completed if the no assignee`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(unassigned)
      .`when`(
        CompleteInteractionTaskCommand(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          assignee = null
        )
      ).expectEvents(
        TaskToBeCompletedEvent(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          formKey = "some"
        )
      )
  }


  @Test
  fun `should not mark task to be completed if already deleted`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(assigned, deleted)
      .`when`(
        CompleteInteractionTaskCommand(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          assignee = "gonzo"
        ))
      .expectNoEvents()
  }

  @Test
  fun `should not mark task to be completed if already completed`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(assigned, completed)
      .`when`(
        CompleteInteractionTaskCommand(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          payload = completionPayload,
          assignee = "gonzo"
        ))
      .expectNoEvents()
  }

}
