package io.holunda.polyflow.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
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
   * React on registration of a new process definition.
   */
  @EventSourcingHandler
  fun on(event: ProcessDefinitionRegisteredEvent) {
    this.processDefinitionId = event.processDefinitionId
  }
}
