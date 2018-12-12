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
  }


  @Test
  fun `should unclaim, claim and mark task to be completed`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(
        TaskCreatedEngineEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          sourceReference = processReference,
          assignee = "kermit",
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ))
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
  fun `should mark task to be completed`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(
        TaskCreatedEngineEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          sourceReference = processReference,
          assignee = "kermit",
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ))
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
      .given(
        TaskCreatedEngineEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          sourceReference = processReference,
          assignee = null,
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ))
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
  fun `should not mark task to be completed if already deleted`() {

    val completionPayload = Variables.createVariables().putValueTyped("user-input", Variables.stringValue("whatever"))

    fixture
      .given(
        TaskCreatedEngineEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          sourceReference = processReference,
          assignee = "kermit",
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ),
        TaskDeletedEngineEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          sourceReference = processReference,
          deleteReason = "Test delete"
        ))
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
      .given(
        TaskCreatedEngineEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          sourceReference = processReference,
          assignee = "kermit",
          candidateUsers = listOf("kermit", "gonzo"),
          candidateGroups = listOf("muppets"),
          priority = 51,
          description = "Funky task",
          payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
          correlations = newCorrelations().addCorrelation("Request", "business123")
        ),
        TaskCompletedEngineEvent(
          id = "4711",
          name = "Foo",
          createTime = now,
          owner = "kermit",
          taskDefinitionKey = "foo",
          formKey = "some",
          businessKey = "business123",
          sourceReference = processReference
        ))
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
