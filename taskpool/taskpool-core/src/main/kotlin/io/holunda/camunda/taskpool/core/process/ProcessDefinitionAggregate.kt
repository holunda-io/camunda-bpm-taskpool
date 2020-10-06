package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class ProcessDefinitionAggregate {

  @AggregateIdentifier
  lateinit var processDefinitionId: String

  @CommandHandler
  fun handle(command: RegisterProcessDefinitionCommand) {
    AggregateLifecycle.apply(ProcessDefinitionRegisteredEvent(
      processDefinitionId = command.processDefinitionId,
      processDefinitionKey = command.processDefinitionKey,
      processDefinitionVersion = command.processDefinitionVersion,
      processDescription = command.processDescription,
      processName = command.processName,
      processVersionTag = command.processVersionTag,
      applicationName = command.applicationName,
      candidateStarterGroups = command.candidateStarterGroups,
      candidateStarterUsers = command.candidateStarterUsers,
      formKey = command.formKey,
      startableFromTasklist = command.startableFromTasklist
    ))
  }


  @EventSourcingHandler
  fun on(event: ProcessDefinitionRegisteredEvent) {
    this.processDefinitionId = event.processDefinitionId
  }
}
