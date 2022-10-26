package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import org.axonframework.test.aggregate.AggregateTestFixture
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Checks assignment engine commands and eventing of it.
 */
class TaskAssignmentChangeTest {

  private val fixture: AggregateTestFixture<TaskAggregate> = AggregateTestFixture(TaskAggregate::class.java)
  private val processReference = ProcessReference(
    definitionKey = "process_key",
    instanceId = "0815",
    executionId = "12345",
    definitionId = "76543",
    name = "My process",
    applicationName = "myExample"
  )

  private lateinit var now: Date
  private lateinit var createdEvent: TaskCreatedEngineEvent
  private lateinit var deletedEvent: TaskDeletedEngineEvent
  private lateinit var completedEvent: TaskCompletedEngineEvent

  @BeforeEach
  fun setUp() {
    now = Date()

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
  }

  @Test
  fun `should event added candidate group`() {
    fixture
      .given(createdEvent)
      .`when`(
        AddCandidateGroupsCommand(
          id = "4711",
          candidateGroups = setOf("nasa")
        )
      ).expectEvents(
        TaskCandidateGroupChanged(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          assignmentUpdateType = CamundaTaskEventType.CANDIDATE_GROUP_ADD,
          groupId = "nasa"
        )
      )
  }

  @Test
  fun `should event added candidate user`() {
    fixture
      .given(createdEvent)
      .`when`(
        AddCandidateUsersCommand(
          id = "4711",
          candidateUsers = setOf("rocketman")
        )
      ).expectEvents(
        TaskCandidateUserChanged(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          assignmentUpdateType = CamundaTaskEventType.CANDIDATE_USER_ADD,
          userId = "rocketman"
        )
      )
  }

  @Test
  fun `should event removed candidate group`() {
    fixture
      .given(createdEvent)
      .`when`(
        DeleteCandidateGroupsCommand(
          id = "4711",
          candidateGroups = setOf("muppetshow")
        )
      ).expectEvents(
        TaskCandidateGroupChanged(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          assignmentUpdateType = CamundaTaskEventType.CANDIDATE_GROUP_DELETE,
          groupId = "muppetshow"
        )
      )
  }

  @Test
  fun `should event removed candidate user`() {
    fixture
      .given(createdEvent)
      .`when`(
        DeleteCandidateUsersCommand(
          id = "4711",
          candidateUsers = setOf("kermit")
        )
      ).expectEvents(
        TaskCandidateUserChanged(
          id = "4711",
          sourceReference = processReference,
          taskDefinitionKey = "foo",
          assignmentUpdateType = CamundaTaskEventType.CANDIDATE_USER_DELETE,
          userId = "kermit"
        )
      )
  }

  @Test
  fun `should not event on completed task`() {
    fixture
      .given(createdEvent, completedEvent)
      .`when`(AddCandidateUsersCommand(
        id = "4711",
        candidateUsers = setOf("rocketman")
      )).expectNoEvents()
    fixture
      .given(createdEvent, completedEvent)
      .`when`(DeleteCandidateUsersCommand(
        id = "4711",
        candidateUsers = setOf("kermit")
      )).expectNoEvents()
    fixture
      .given(createdEvent, completedEvent)
      .`when`(DeleteCandidateGroupsCommand(
        id = "4711",
        candidateGroups = setOf("muppetshow")
      )).expectNoEvents()
    fixture
      .given(createdEvent, completedEvent)
      .`when`(AddCandidateGroupsCommand(
        id = "4711",
        candidateGroups = setOf("nasa")
      )).expectNoEvents()


  }

  @Test
  fun `should not event on deleted task`() {

    fixture
      .given(createdEvent, deletedEvent)
      .`when`(AddCandidateUsersCommand(
        id = "4711",
        candidateUsers = setOf("rocketman")
      )).expectNoEvents()
    fixture
      .given(createdEvent, deletedEvent)
      .`when`(DeleteCandidateUsersCommand(
        id = "4711",
        candidateUsers = setOf("kermit")
      )).expectNoEvents()
    fixture
      .given(createdEvent, deletedEvent)
      .`when`(DeleteCandidateGroupsCommand(
        id = "4711",
        candidateGroups = setOf("muppetshow")
      )).expectNoEvents()
    fixture
      .given(createdEvent, deletedEvent)
      .`when`(AddCandidateGroupsCommand(
        id = "4711",
        candidateGroups = setOf("nasa")
      )).expectNoEvents()

  }

}
