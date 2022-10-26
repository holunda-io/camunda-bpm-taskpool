package io.holunda.camunda.taskpool.mapper.process

import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.api.process.variable.ChangeProcessVariablesForExecutionCommand
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariablesChangedEvent

fun StartProcessInstanceCommand.startedEvent() = ProcessInstanceStartedEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  startUserId = this.startUserId,
  startActivityId = this.startActivityId,
  superInstanceId = this.superInstanceId
)

fun FinishProcessInstanceCommand.finishedEvent() = ProcessInstanceEndedEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  endActivityId = this.endActivityId,
  superInstanceId = this.superInstanceId
)

fun ResumeProcessInstanceCommand.resumedEvent() = ProcessInstanceResumedEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
)

fun SuspendProcessInstanceCommand.suspendedEvent() = ProcessInstanceSuspendedEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
)

fun CancelProcessInstanceCommand.cancelledEvent() = ProcessInstanceCancelledEvent(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  endActivityId = this.endActivityId,
  superInstanceId = this.superInstanceId,
  deleteReason = this.deleteReason
)

fun ChangeProcessVariablesForExecutionCommand.toVariablesChangedEvent() = ProcessVariablesChangedEvent(
  sourceReference = this.sourceReference,
  variableChanges = this.variableChanges
)

