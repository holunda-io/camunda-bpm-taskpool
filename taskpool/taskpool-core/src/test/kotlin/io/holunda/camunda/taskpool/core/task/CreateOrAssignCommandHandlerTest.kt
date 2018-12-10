package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.CreateOrAssignTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventsourcing.EventSourcingRepository
import org.camunda.bpm.engine.variable.Variables
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.time.Instant
import java.util.*

class CreateOrAssignCommandHandlerTest {

  @get: Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  private val processReference = ProcessReference(
    definitionKey = "process_key",
    instanceId = "0815",
    executionId = "12345",
    definitionId = "76543",
    name = "My process",
    applicationName = "myExample"
  )

  @Mock
  private lateinit var eventSourcingRepository: EventSourcingRepository<TaskAggregate>

  private lateinit var createOrAssignTaskCommand: CreateOrAssignTaskCommand
  private lateinit var now: Date
  private lateinit var now2: Date
  private lateinit var createOrAssignCommandHandler: CreateOrAssignCommandHandler

  @Before
  fun setUp() {
    now = Date()
    now2 = Date.from(Instant.now().plusSeconds(1000))
    createOrAssignTaskCommand = CreateOrAssignTaskCommand(
      id = "4711",
      name = "Foo",
      createTime = now,
      eventName = "create",
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      enriched = true,
      sourceReference = processReference,
      candidateUsers = listOf("kermit", "gonzo"),
      candidateGroups = listOf("muppets"),
      assignee = "kermit",
      priority = 51,
      followUpDate = now2,
      dueDate = now,
      description = "Funky task",
      payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
      correlations = newCorrelations().addCorrelation("Request", "business123")
    )

    createOrAssignCommandHandler = CreateOrAssignCommandHandler(eventSourcingRepository)
  }


  @Test
  fun `should create TaskCreate command`() {

    val command = createOrAssignCommandHandler.create(createOrAssignTaskCommand)

    assertThat(command.assignee).isEqualTo(createOrAssignTaskCommand.assignee)
    assertThat(command.businessKey).isEqualTo(createOrAssignTaskCommand.businessKey)
    assertThat(command.candidateGroups).isEqualTo(createOrAssignTaskCommand.candidateGroups)
    assertThat(command.candidateUsers).isEqualTo(createOrAssignTaskCommand.candidateUsers)
    assertThat(command.correlations).isEqualTo(createOrAssignTaskCommand.correlations)
    assertThat(command.createTime).isEqualTo(createOrAssignTaskCommand.createTime)
    assertThat(command.description).isEqualTo(createOrAssignTaskCommand.description)
    assertThat(command.dueDate).isEqualTo(createOrAssignTaskCommand.dueDate)
    assertThat(command.enriched).isEqualTo(createOrAssignTaskCommand.enriched)
    assertThat(command.eventName).isEqualTo(createOrAssignTaskCommand.eventName)
    assertThat(command.followUpDate).isEqualTo(createOrAssignTaskCommand.followUpDate)
    assertThat(command.formKey).isEqualTo(createOrAssignTaskCommand.formKey)
    assertThat(command.id).isEqualTo(createOrAssignTaskCommand.id)
    assertThat(command.name).isEqualTo(createOrAssignTaskCommand.name)
    assertThat(command.owner).isEqualTo(createOrAssignTaskCommand.owner)
    assertThat(command.payload).isEqualTo(createOrAssignTaskCommand.payload)
    assertThat(command.priority).isEqualTo(createOrAssignTaskCommand.priority)
    assertThat(command.sourceReference).isEqualTo(createOrAssignTaskCommand.sourceReference)
    assertThat(command.taskDefinitionKey).isEqualTo(createOrAssignTaskCommand.taskDefinitionKey)
  }

  @Test
  fun `should create TaskAssign command`() {

    val command = createOrAssignCommandHandler.assign(createOrAssignTaskCommand)

    assertThat(command.assignee).isEqualTo(createOrAssignTaskCommand.assignee)
    assertThat(command.businessKey).isEqualTo(createOrAssignTaskCommand.businessKey)
    assertThat(command.candidateGroups).isEqualTo(createOrAssignTaskCommand.candidateGroups)
    assertThat(command.candidateUsers).isEqualTo(createOrAssignTaskCommand.candidateUsers)
    assertThat(command.correlations).isEqualTo(createOrAssignTaskCommand.correlations)
    assertThat(command.createTime).isEqualTo(createOrAssignTaskCommand.createTime)
    assertThat(command.description).isEqualTo(createOrAssignTaskCommand.description)
    assertThat(command.dueDate).isEqualTo(createOrAssignTaskCommand.dueDate)
    assertThat(command.enriched).isEqualTo(createOrAssignTaskCommand.enriched)
    assertThat(command.eventName).isEqualTo(createOrAssignTaskCommand.eventName)
    assertThat(command.followUpDate).isEqualTo(createOrAssignTaskCommand.followUpDate)
    assertThat(command.formKey).isEqualTo(createOrAssignTaskCommand.formKey)
    assertThat(command.id).isEqualTo(createOrAssignTaskCommand.id)
    assertThat(command.name).isEqualTo(createOrAssignTaskCommand.name)
    assertThat(command.owner).isEqualTo(createOrAssignTaskCommand.owner)
    assertThat(command.payload).isEqualTo(createOrAssignTaskCommand.payload)
    assertThat(command.priority).isEqualTo(createOrAssignTaskCommand.priority)
    assertThat(command.sourceReference).isEqualTo(createOrAssignTaskCommand.sourceReference)
    assertThat(command.taskDefinitionKey).isEqualTo(createOrAssignTaskCommand.taskDefinitionKey)
  }

}
