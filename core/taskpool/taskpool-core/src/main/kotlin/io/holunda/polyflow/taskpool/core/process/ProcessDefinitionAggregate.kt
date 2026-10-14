package io.holunda.polyflow.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.SuspendProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.definition.ActivateProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.definition.UnregisterProcessDefinitionCommand
import io.holunda.camunda.taskpool.mapper.process.suspendEvent
import io.holunda.camunda.taskpool.mapper.process.activateEvent
import io.holunda.camunda.taskpool.mapper.process.unregisterEvent
import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import io.holunda.camunda.taskpool.mapper.process.registerEvent
import io.holunda.polyflow.taskpool.core.TaskPoolCoreConfiguration
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

/**
 * Command model responsible for commands on process definition.
 */
@Aggregate(repository = TaskPoolCoreConfiguration.PROCESS_DEFINITION_AGGREGATE_REPOSITORY)
class ProcessDefinitionAggregate() {

  @AggregateIdentifier
  lateinit var processDefinitionId: String

  /**
   * Handles registration, no [CommandHandler] annotation, since we use an external handler.
   * @param command registration command.
   */
  fun handle(command: RegisterProcessDefinitionCommand) {
    AggregateLifecycle.apply(command.registerEvent())
  }

  /**
   * Applies the lifecycle event for a suspended definition.
   */
  fun handle(command: SuspendProcessDefinitionCommand) {
    AggregateLifecycle.apply(command.suspendEvent())
  }

  /**
   * Applies the lifecycle event for an activated definition.
   */
  fun handle(command: ActivateProcessDefinitionCommand) {
    AggregateLifecycle.apply(command.activateEvent())
  }

  /**
   * Applies the lifecycle event for an unregistered definition.
   */
  fun handle(command: UnregisterProcessDefinitionCommand) {
    AggregateLifecycle.apply(command.unregisterEvent())
  }

  /**
   * React on registration of a new process definition.
   */
  @EventSourcingHandler
  fun on(event: ProcessDefinitionRegisteredEvent) {
    this.processDefinitionId = event.processDefinitionId
  }
}
