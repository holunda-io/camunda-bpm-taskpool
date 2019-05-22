package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.TaskSenderProperties
import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.sender.gateway.AxonCommandListGateway
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.GenericCommandResultMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit

class AxonCommandListGatewayTest {

  @get: Rule
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var commandGateway: CommandGateway

  @Test
  fun `should not send commands if disabled by property`() {
    val wrapper = AxonCommandListGateway(commandGateway, TaskCollectorProperties(
      springApplicationName = "some-name",
      sender = TaskSenderProperties(
        enabled = false
      )
    ))

    wrapper.sendToGateway(listOf(makeCreateTaskCommand()))

    verifyNoMoreInteractions(commandGateway)
  }

  @Test
  fun `should send commands in sequence`() {
    val wrapper = AxonCommandListGateway(commandGateway, TaskCollectorProperties(
      springApplicationName = "some-name",
      sender = TaskSenderProperties(
        enabled = true
      )
    ))

    val createTaskCommand = makeCreateTaskCommand()
    val assignTaskCommand = AssignTaskCommand(
      id = "some-id",
      assignee = "kermit"
    )
    @Suppress("UNCHECKED_CAST")
    val callbackMatcher: ArgumentCaptor<CommandCallback<CreateTaskCommand, String>> =
      ArgumentCaptor.forClass(CommandCallback::class.java) as ArgumentCaptor<CommandCallback<CreateTaskCommand, String>>

    Mockito.doNothing().`when`(commandGateway).send(eq(createTaskCommand), callbackMatcher.capture())

    // send the commands
    wrapper.sendToGateway(listOf(createTaskCommand, assignTaskCommand))

    // and verify that (only) the first asState the two was sent
    verify(commandGateway).send(eq(createTaskCommand), any() as? CommandCallback<CreateTaskCommand, *>)
    verifyNoMoreInteractions(commandGateway)

    // for the other one to be sent, we have to manually trigger the command callback ourselves since we mock the Axon command gateway
    callbackMatcher.value.onResult(GenericCommandMessage.asCommandMessage(createTaskCommand), GenericCommandResultMessage("some-id"))

    // verify that the other command was send, too
    verify(commandGateway).send(eq(assignTaskCommand), any() as? CommandCallback<AssignTaskCommand, *>)
    verifyNoMoreInteractions(commandGateway)
  }

  private fun makeCreateTaskCommand() =
    CreateTaskCommand(
      id = "some-id",
      sourceReference = ProcessReference(
        "instance-id-12345",
        "execution-id-12345",
        "definition-id-12345",
        "definition-key-abcde",
        "process-name",
        "application-name"
      ),
      taskDefinitionKey = "task-definition-key-abcde"
    )
}
