package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.variable.*
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

/**
 * Main representation of the process variable available in the system.
 */
@Aggregate
class ProcessVariableAggregate() {

  companion object : KLogging()

  @AggregateIdentifier
  private lateinit var variableInstanceId: String

  @CommandHandler
  constructor(cmd: CreateProcessVariableCommand) : this() {
    AggregateLifecycle.apply(ProcessVariableCreatedEvent(
      sourceReference = cmd.sourceReference,
      variableInstanceId = cmd.variableInstanceId,
      scopeActivityInstanceId = cmd.scopeActivityInstanceId,
      variableName = cmd.variableName,
      value = cmd.value
    ))
  }

  fun update(cmd: UpdateProcessVariableCommand) {
    AggregateLifecycle.apply(ProcessVariableUpdatedEvent(
      sourceReference = cmd.sourceReference,
      variableInstanceId = cmd.variableInstanceId,
      scopeActivityInstanceId = cmd.scopeActivityInstanceId,
      variableName = cmd.variableName,
      value = cmd.value
    ))
  }

  @CommandHandler
  fun delete(cmd: DeleteProcessVariableCommand) {
    AggregateLifecycle.apply(ProcessVariableDeletedEvent(
      sourceReference = cmd.sourceReference,
      variableInstanceId = cmd.variableInstanceId,
      scopeActivityInstanceId = cmd.scopeActivityInstanceId,
      variableName = cmd.variableName
    ))
  }

  @EventSourcingHandler
  fun on(event: ProcessVariableCreatedEvent) {
    this.variableInstanceId = event.variableInstanceId
  }

  @EventSourcingHandler
  fun on(event: ProcessVariableUpdatedEvent) {
    if (!this::variableInstanceId.isInitialized) {
      this.variableInstanceId = event.variableInstanceId
    }
  }

}
