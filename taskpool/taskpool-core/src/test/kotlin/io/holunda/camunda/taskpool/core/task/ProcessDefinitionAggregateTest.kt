package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.task.RegisterProcessDefinitionCommand
import io.holunda.camunda.taskpool.core.process.ProcessDefinitionAggregate
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.Test

class ProcessDefinitionAggregateTest {

  val fixture = AggregateTestFixture<ProcessDefinitionAggregate>(ProcessDefinitionAggregate::class.java)


  @Test
  fun `should create aggregate`() {

    val command = RegisterProcessDefinitionCommand(
      "id:1",
      "id",
      1,
      "my application",
      "my process",
      "MVP",
      "This is a very nice process",
      "startForm",
      true,
      setOf("kermit"),
      setOf("muppetshow")
    )

    fixture
      .givenNoPriorActivity()
      .`when`(command)
      .expectEvents(ProcessDefinitionRegisteredEvent(
        command.processDefinitionId,
        command.processDefinitionKey,
        command.processDefinitionVersion,
        command.applicationName,
        command.processName,
        command.processVersionTag,
        command.processDescription,
        command.formKey,
        command.startableFromTasklist,
        command.candidateStarterUsers,
        command.candidateStarterGroups
      ))
  }
}
