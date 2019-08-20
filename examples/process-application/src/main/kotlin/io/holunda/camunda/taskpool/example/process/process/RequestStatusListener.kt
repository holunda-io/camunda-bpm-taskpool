package io.holunda.camunda.taskpool.example.process.process

import io.holunda.camunda.datapool.sender.DataEntryCommandSender
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType.*
import io.holunda.camunda.taskpool.collector.TaskEventCollectorService
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Values.APPROVE
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Values.CANCEL
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Values.REJECT
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Values.RESUBMIT
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Values.RETURN
import io.holunda.camunda.taskpool.example.process.service.BusinessDataEntry
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.example.process.service.RequestService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class RequestStatusListener(
  private val sender: DataEntryCommandSender,
  private val requestService: RequestService
) {

  @EventListener(condition = "#task.eventName.equals('complete')")
  @Order(TaskEventCollectorService.ORDER - 11)
  fun notifyRequestStatusChange(task: DelegateTask) {
    val approvalDecision = task.getVariable(ProcessApproveRequest.Variables.APPROVE_DECISION) as String?
    val amendAction = task.getVariable(ProcessApproveRequest.Variables.AMEND_ACTION) as String?

    val id: String = task.getVariable(ProcessApproveRequest.Variables.REQUEST_ID) as String
    val request: Request = requestService.getRequest(id)

    if (amendAction.isNullOrBlank()) {
      when (approvalDecision) {
        APPROVE -> {
          notifyChange(request = request, task = task, state = COMPLETED.of("Approved"), log = "Request successfully approved.")
        }
        REJECT -> {
          notifyChange(request = request, task = task, state = CANCELLED.of("Rejected"), log = "Request rejected by approver.")
        }
        RETURN -> {
          notifyChange(request = request, task = task, state = IN_PROGRESS.of("Returned"), log = "Request returned to originator.")
        }
        else -> Unit
      }
    } else {
      when (amendAction) {
        RESUBMIT -> {
          notifyChange(request = request, task = task, state = IN_PROGRESS.of("Resubmitted"), log = "Request resubmitted for approval.")
        }
        CANCEL -> {
          notifyChange(request = request, task = task, state = IN_PROGRESS.of("Cancelled"), log = "Request cancelled by originator.")
        }
        else -> Unit
      }
    }
  }

  fun notifyChange(request: Request, task: DelegateTask, state: DataEntryState, log: String, logNotes: String? = null) {
    sender.sendDataEntryCommand(
      entryType = BusinessDataEntry.REQUEST,
      entryId = request.id,
      payload = request,
      state = state,
      name = "AR ${request.id}",
      description = request.subject,
      type = "Approval Request",
      modification = Modification(
        time = OffsetDateTime.now(),
        username = task.assignee,
        log = log,
        logNotes = logNotes
      ),
      authorizations = listOf(addUser(task.assignee), addUser(request.applicant))
    )
  }
}


