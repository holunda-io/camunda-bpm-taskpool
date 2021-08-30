package io.holunda.polyflow.example.process.approval.process.listener

import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.APPLICANT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.ORIGINATOR
import io.holunda.polyflow.example.process.approval.service.RequestService
import io.holunda.polyflow.taskpool.collector.task.TaskEventCollectorService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Example description setting listener to demonstrate that task pool is aware of listener order.
 */
@Component
class DescriptionSettingTaskListener(
  private val requestService: RequestService
) {

  @EventListener(condition = "#task.eventName.equals('create')")
  @Order(TaskEventCollectorService.ORDER - 10)
  fun changeDescription(task: DelegateTask) {

    task.description = when (task.taskDefinitionKey) {
      RequestApprovalProcess.Elements.APPROVE_REQUEST -> "Please approve request ${task.execution.businessKey} from ${ORIGINATOR.from(task).get()} on behalf of ${APPLICANT.from(task).get()}."
      RequestApprovalProcess.Elements.AMEND_REQUEST -> "Please amend the approval request ${task.execution.businessKey}."
      else -> ""
    }
  }
}
