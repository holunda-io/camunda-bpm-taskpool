package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.instance.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class ProcessInstanceAggregate() {

  @AggregateIdentifier
  lateinit var processInstanceId: String

  @CommandHandler
  constructor(cmd: StartProcessInstanceCommand): this() {
    AggregateLifecycle.apply(ProcessInstanceStartedEvent(
      processInstanceId = cmd.processInstanceId,
      sourceReference = cmd.sourceReference,
      businessKey = cmd.businessKey,
      startUserId = cmd.startUserId,
      startActivityId = cmd.startActivityId,
      superInstanceId = cmd.superInstanceId
    ))
  }

  @CommandHandler
  fun end(cmd: FinishProcessInstanceCommand) {
    AggregateLifecycle.apply(ProcessInstanceEndedEvent(
      processInstanceId = cmd.processInstanceId,
      sourceReference = cmd.sourceReference,
      businessKey = cmd.businessKey,
      endActivityId = cmd.endActivityId,
      superInstanceId = cmd.superInstanceId
    ))
  }

  @CommandHandler
  fun cancel(cmd: CancelProcessInstanceCommand) {
    AggregateLifecycle.apply(ProcessInstanceCancelledEvent(
      processInstanceId = cmd.processInstanceId,
      sourceReference = cmd.sourceReference,
      businessKey = cmd.businessKey,
      endActivityId = cmd.endActivityId,
      superInstanceId = cmd.superInstanceId,
      deleteReason = cmd.deleteReason
    ))
  }

  @CommandHandler
  fun suspend(cmd: SuspendProcessInstanceCommand) {
    AggregateLifecycle.apply(ProcessInstanceSuspendedEvent(
      processInstanceId = cmd.processInstanceId,
      sourceReference = cmd.sourceReference,
    ))
  }

  @CommandHandler
  fun resume(cmd: ResumeProcessInstanceCommand) {
    AggregateLifecycle.apply(ProcessInstanceResumedEvent(
      processInstanceId = cmd.processInstanceId,
      sourceReference = cmd.sourceReference,
    ))
  }


  @EventSourcingHandler
  fun on(event: ProcessInstanceStartedEvent) {
    this.processInstanceId = event.processInstanceId
  }
}
