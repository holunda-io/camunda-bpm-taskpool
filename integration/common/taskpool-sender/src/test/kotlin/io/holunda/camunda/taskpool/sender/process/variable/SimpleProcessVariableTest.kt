package io.holunda.camunda.taskpool.sender.process.variable

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.process.variable.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.configureTaskpoolJacksonObjectMapper
import io.holunda.camunda.taskpool.sender.SenderProperties
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.engine.variable.impl.value.builder.ObjectVariableBuilderImpl
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import java.util.*


class SimpleProcessVariableTest {

  val store: MutableList<Any> = mutableListOf()

  private val commandListGateway: CommandListGateway = object : CommandListGateway {
    override fun sendToGateway(commands: List<Any>) {
      store.addAll(commands)
    }
  }
  private val objectMapper = jacksonObjectMapper().configureTaskpoolJacksonObjectMapper()
  private val simpleProcessVariableCommandSender = SimpleProcessVariableCommandSender(commandListGateway, SenderProperties(), objectMapper)

  @BeforeEach
  fun reset() {
    store.clear()
  }

  @Test
  fun `should serialize complex variable on create`() {

    val value = MyObject("value1", 67)

    val variables = createVariables()
      .putValueTyped("complex", ObjectVariableBuilderImpl(value).serializationDataFormat(Variables.SerializationDataFormats.JSON).create())

    val cmd = CreateSingleProcessVariableCommand(
      variableInstanceId = UUID.randomUUID().toString(),
      variableName = "complex",
      sourceReference = ProcessReference(
        instanceId = "d7c7c8cd-0475-11e9-90f1-a0c589a3e9e5",
        executionId = "d7c7c8cd-0475-11e9-90f1-a0c589a3e9e5",
        definitionId = "processId:4:d7c6de6c-0475-11e9-90f1-a0c589a3e9e5",
        name = "My Process",
        definitionKey = "processId",
        applicationName = "command-projector-test"
      ),
      scopeActivityInstanceId = UUID.randomUUID().toString(),
      value = TypedValueProcessVariableValue(variables.getValueTyped("complex")),
      revision = 1
    )
    simpleProcessVariableCommandSender.send(cmd)

    assertThat(store).hasSize(1)
    assertThat(store[0]).isInstanceOf(ChangeProcessVariablesForExecutionCommand::class.java)
    val command = store[0] as ChangeProcessVariablesForExecutionCommand
    assertThat(command.variableChanges).hasSize(1)
    assertThat(command.variableChanges[0]).isInstanceOf(ProcessVariableCreate::class.java)

    val change = command.variableChanges[0] as ProcessVariableCreate
    assertThat(change.value).isInstanceOf(ObjectProcessVariableValue::class.java)
    assertThat(change.value.value).isInstanceOf(Map::class.java)
    @Suppress("UNCHECKED_CAST")
    assertThat(change.value.value as Map<String, Any>).containsExactlyInAnyOrderEntriesOf(mapOf("prop1" to "value1", "prop2" to 67))
  }


  @Test
  fun `should serialize complex variable on update`() {

    val value = MyObject("value1", 67)

    val variables = createVariables()
      .putValueTyped("complex", ObjectVariableBuilderImpl(value).serializationDataFormat(Variables.SerializationDataFormats.JSON).create())

    val cmd = UpdateSingleProcessVariableCommand(
      variableInstanceId = UUID.randomUUID().toString(),
      variableName = "complex",
      sourceReference = ProcessReference(
        instanceId = "d7c7c8cd-0475-11e9-90f1-a0c589a3e9e5",
        executionId = "d7c7c8cd-0475-11e9-90f1-a0c589a3e9e5",
        definitionId = "processId:4:d7c6de6c-0475-11e9-90f1-a0c589a3e9e5",
        name = "My Process",
        definitionKey = "processId",
        applicationName = "command-projector-test"
      ),
      scopeActivityInstanceId = UUID.randomUUID().toString(),
      value = TypedValueProcessVariableValue(variables.getValueTyped("complex")),
      revision = 1
    )
    simpleProcessVariableCommandSender.send(cmd)

    assertThat(store).hasSize(1)
    assertThat(store[0]).isInstanceOf(ChangeProcessVariablesForExecutionCommand::class.java)
    val command = store[0] as ChangeProcessVariablesForExecutionCommand
    assertThat(command.variableChanges).hasSize(1)
    assertThat(command.variableChanges[0]).isInstanceOf(ProcessVariableUpdate::class.java)

    val change = command.variableChanges[0] as ProcessVariableUpdate

    assertThat(change.value).isInstanceOf(ObjectProcessVariableValue::class.java)
    assertThat(change.value.value).isInstanceOf(Map::class.java)
    @Suppress("UNCHECKED_CAST")
    assertThat(change.value.value as Map<String, Any>).containsExactlyInAnyOrderEntriesOf(mapOf("prop1" to "value1", "prop2" to 67))
  }

}

data class MyObject(val prop1: String, val prop2: Int)

