package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.instance.EndProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceEndedEvent
import io.holunda.camunda.taskpool.api.process.instance.StartProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceStartedEvent
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
  fun end(cmd: EndProcessInstanceCommand) {
    AggregateLifecycle.apply(ProcessInstanceEndedEvent(
      processInstanceId = cmd.processInstanceId,
      sourceReference = cmd.sourceReference,
      businessKey = cmd.businessKey,
      endActivityId = cmd.endActivityId,
      deleteReason = cmd.deleteReason,
      superInstanceId = cmd.superInstanceId
    ))
  }

  @EventSourcingHandler
  fun on(event: ProcessInstanceStartedEvent) {
    this.processInstanceId = event.processInstanceId
  }
}
