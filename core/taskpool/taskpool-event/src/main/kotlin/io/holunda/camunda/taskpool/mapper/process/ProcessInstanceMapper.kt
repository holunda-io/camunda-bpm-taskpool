package io.holunda.camunda.taskpool.mapper.process

import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.api.process.variable.ChangeProcessVariablesForExecutionCommand
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariablesChangedEvent

/**
 * Maps command to event.
 */
fun StartProcessInstanceCommand.startedEvent() = ProcessInstanceStartedEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  startUserId = this.startUserId,
  startActivityId = this.startActivityId,
  superInstanceId = this.superInstanceId
)

/**
 * Maps command to event.
 */
fun FinishProcessInstanceCommand.finishedEvent() = ProcessInstanceEndedEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  endActivityId = this.endActivityId,
  superInstanceId = this.superInstanceId
)

/**
 * Maps command to event.
 */
fun ResumeProcessInstanceCommand.resumedEvent() = ProcessInstanceResumedEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
)

/**
 * Maps command to event.
 */
fun SuspendProcessInstanceCommand.suspendedEvent() = ProcessInstanceSuspendedEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
)

/**
 * Maps command to event.
 */
fun CancelProcessInstanceCommand.cancelledEvent() = ProcessInstanceCancelledEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  endActivityId = this.endActivityId,
  superInstanceId = this.superInstanceId,
  deleteReason = this.deleteReason
)

/**
 * Maps command to event.
 */
fun ChangeProcessVariablesForExecutionCommand.toVariablesChangedEvent() = ProcessVariablesChangedEvent(
  sourceReference = this.sourceReference,
  variableChanges = this.variableChanges
)

