package io.holunda.polyflow.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.api.process.variable.ChangeProcessVariablesForExecutionCommand
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariablesChangedEvent
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

/**
 * Aggregate representing the process instance.
 */
@Aggregate
class ProcessInstanceAggregate() {

  companion object : KLogging()

  @AggregateIdentifier
  lateinit var processInstanceId: String

  /**
   * Create instance handler.
   */
  @CommandHandler
  constructor(cmd: StartProcessInstanceCommand) : this() {
    logger.debug { "Process instance started ${cmd.processInstanceId}, application: ${cmd.sourceReference.applicationName}." }
    AggregateLifecycle.apply(
      ProcessInstanceStartedEvent(
        processInstanceId = cmd.processInstanceId,
        sourceReference = cmd.sourceReference,
        businessKey = cmd.businessKey,
        startUserId = cmd.startUserId,
        startActivityId = cmd.startActivityId,
        superInstanceId = cmd.superInstanceId
      )
    )
  }

  /**
   * Finish instance handler.
   */
  @CommandHandler
  fun finish(cmd: FinishProcessInstanceCommand) {
    logger.debug { "Process instance finished ${cmd.processInstanceId}, application: ${cmd.sourceReference.applicationName}." }
    AggregateLifecycle.apply(
      ProcessInstanceEndedEvent(
        processInstanceId = cmd.processInstanceId,
        sourceReference = cmd.sourceReference,
        businessKey = cmd.businessKey,
        endActivityId = cmd.endActivityId,
        superInstanceId = cmd.superInstanceId
      )
    )
  }

  /**
   * Cancel instance handler (by user).
   */
  @CommandHandler
  fun cancel(cmd: CancelProcessInstanceCommand) {
    logger.debug { "Process instance cancelled ${cmd.processInstanceId}, application: ${cmd.sourceReference.applicationName}." }
    AggregateLifecycle.apply(
      ProcessInstanceCancelledEvent(
        processInstanceId = cmd.processInstanceId,
        sourceReference = cmd.sourceReference,
        businessKey = cmd.businessKey,
        endActivityId = cmd.endActivityId,
        superInstanceId = cmd.superInstanceId,
        deleteReason = cmd.deleteReason
      )
    )
  }

  /**
   * Suspend instance handler (by user).
   */
  @CommandHandler
  fun suspend(cmd: SuspendProcessInstanceCommand) {
    logger.debug { "Process instance suspended ${cmd.processInstanceId}, application: ${cmd.sourceReference.applicationName}." }
    AggregateLifecycle.apply(
      ProcessInstanceSuspendedEvent(
        processInstanceId = cmd.processInstanceId,
        sourceReference = cmd.sourceReference,
      )
    )
  }

  /**
   * Resume instance handler (by user).
   */
  @CommandHandler
  fun resume(cmd: ResumeProcessInstanceCommand) {
    logger.debug { "Process instance resumed ${cmd.processInstanceId}, application: ${cmd.sourceReference.applicationName}." }
    AggregateLifecycle.apply(
      ProcessInstanceResumedEvent(
        processInstanceId = cmd.processInstanceId,
        sourceReference = cmd.sourceReference,
      )
    )
  }


  /**
   * Process variables of an execution of this process instance has changed.
   *
   * No [CommandHandler] annotation, see [ProcessInstanceVariablesChangeHandler].
   */
  fun changeVariables(cmd: ChangeProcessVariablesForExecutionCommand) {
    logger.debug {
      "Variables: [${
        cmd.variableChanges.joinToString(",") { it.variableName }
      }] changed for process instance ${cmd.processInstanceId}, application: ${cmd.sourceReference.applicationName}."
    }
    AggregateLifecycle.apply(
      ProcessVariablesChangedEvent(
        sourceReference = cmd.sourceReference,
        variableChanges = cmd.variableChanges
      )
    )
  }

  /**
   * Set process instance id.
   */
  @EventSourcingHandler
  fun on(event: ProcessInstanceStartedEvent) {
    this.processInstanceId = event.processInstanceId
  }

  /**
   * Set process instance id on variable change, if we missed the instance creation.
   */
  @EventSourcingHandler
  fun on(event: ProcessVariablesChangedEvent) {
    if (!this::processInstanceId.isInitialized) {
      this.processInstanceId = event.sourceReference.instanceId
    }
  }
}
