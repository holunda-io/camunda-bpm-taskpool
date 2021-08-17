package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

/**
 * Command model responsible for commands on process definition.
 */
@Aggregate
class ProcessDefinitionAggregate() {

  @AggregateIdentifier
  lateinit var processDefinitionId: String

  /**
   * Handles registration, no [CommandHandler] annotation, since we use an external handler.
   * @param command registration command.
   */
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

  /**
   * React on registration of a new process definition.
   */
  @EventSourcingHandler
  fun on(event: ProcessDefinitionRegisteredEvent) {
    this.processDefinitionId = event.processDefinitionId
  }
}
