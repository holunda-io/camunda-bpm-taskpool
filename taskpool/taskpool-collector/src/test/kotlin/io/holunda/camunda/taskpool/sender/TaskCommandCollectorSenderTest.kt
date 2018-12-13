package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import org.camunda.bpm.engine.variable.Variables
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import java.time.Instant
import java.util.*

class TaskCommandCollectorSenderTest {

  @get: Rule
  val mockitoRule = MockitoJUnit.rule()

  private val processReference = ProcessReference(
    definitionKey = "process_key",
    instanceId = "0815",
    executionId = "12345",
    definitionId = "76543",
    name = "My process",
    applicationName = "myExample"
  )

  @Mock
  private lateinit var sender: CommandSender
  private lateinit var taskCommandCollectorSender: TaskCommandCollectorSender
  private lateinit var now: Date
  private lateinit var now2: Date

  @Before
  fun init() {
    taskCommandCollectorSender = TaskCommandCollectorSender(sender)
    now = Date()
    now2 = Date.from(Instant.now().plusSeconds(1000))

  }

  @Test
  fun `should send init command on create`() {

    val command = CreateTaskCommand(
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


    taskCommandCollectorSender.sendTaskCommand(command)

    verify(sender).send(
      InitialTaskCommand(
        id = command.id,
        name = command.name,
        createTime = command.createTime,
        eventName = command.eventName,
        owner = command.owner,
        taskDefinitionKey = command.taskDefinitionKey,
        formKey = command.formKey,
        businessKey = command.businessKey,
        enriched = command.enriched,
        sourceReference = command.sourceReference,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        priority = command.priority,
        followUpDate = command.followUpDate,
        dueDate = command.dueDate,
        description = command.description,
        payload = command.payload,
        correlations = command.correlations
      )
    )
  }

  @Test
  fun `should send init command on update attributes`() {

    val command = UpdateAttributeTaskCommand(
      id = "4711",
      name = "Foo",
      owner = "kermit",
      taskDefinitionKey = "foo",
      sourceReference = processReference,
      assignee = "kermit",
      priority = 51,
      followUpDate = now2,
      dueDate = now,
      description = "Funky task"
    )


    taskCommandCollectorSender.sendTaskCommand(command)

    verify(sender).send(
      InitialTaskCommand(
        id = command.id,
        name = command.name,
        createTime = command.createTime,
        eventName = command.eventName,
        owner = command.owner,
        taskDefinitionKey = command.taskDefinitionKey,
        sourceReference = command.sourceReference,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        priority = command.priority,
        followUpDate = command.followUpDate,
        dueDate = command.dueDate,
        description = command.description
      )
    )
  }

  @Test
  fun `should send init command on assign`() {

    val command = AssignTaskCommand(
      id = "4711",
      name = "Foo",
      createTime = now,
      eventName = "assign",
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


    taskCommandCollectorSender.sendTaskCommand(command)

    verify(sender).send(
      InitialTaskCommand(
        id = command.id,
        name = command.name,
        createTime = command.createTime,
        eventName = command.eventName,
        owner = command.owner,
        taskDefinitionKey = command.taskDefinitionKey,
        formKey = command.formKey,
        businessKey = command.businessKey,
        enriched = command.enriched,
        sourceReference = command.sourceReference,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        priority = command.priority,
        followUpDate = command.followUpDate,
        dueDate = command.dueDate,
        description = command.description,
        payload = command.payload,
        correlations = command.correlations
      )
    )
  }

  @Test
  fun `should send update assignment command`() {

    val command = AddCandidateGroupCommand(
      id = "4711",
      groupId = "myGroup"
    )
    taskCommandCollectorSender.sendTaskCommand(command)
    verify(sender).send(command)
  }

  @Test
  fun `should send delete command`() {

    val command = DeleteTaskCommand(
      id = "4711",
      name = "Foo",
      createTime = now,
      eventName = "assign",
      owner = "kermit",
      taskDefinitionKey = "foo",
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
      correlations = newCorrelations().addCorrelation("Request", "business123"),
      deleteReason = "deleted"
    )

    taskCommandCollectorSender.sendTaskCommand(command)
    verify(sender).send(command)
  }

}
