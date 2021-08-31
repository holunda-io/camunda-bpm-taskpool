package io.holunda.polyflow.example.process.approval.process.listener

import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.bpm.data.CamundaBpmData.reader
import io.holunda.camunda.taskpool.api.business.AuthorizationChange
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType.COMPLETED
import io.holunda.camunda.taskpool.api.business.ProcessingType.IN_PROGRESS
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Elements.AMEND_REQUEST
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Elements.APPROVE_REQUEST
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Elements.AUDIT_SUBMITTED
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Values.APPROVE
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Values.CANCEL
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Values.REJECT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Values.RESUBMIT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Values.RETURN
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.AMEND_ACTION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.APPROVE_DECISION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.ORIGINATOR
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.PROJECTION_REVISION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.REQUEST_ID
import io.holunda.polyflow.example.process.approval.service.BusinessDataEntry
import io.holunda.polyflow.example.process.approval.service.Request
import io.holunda.polyflow.example.process.approval.service.RequestService
import io.holunda.polyflow.datapool.sender.DataEntryCommandSender
import io.holunda.polyflow.taskpool.collector.task.TaskEventCollectorService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.VariableScope
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class RequestStatusListener(
  private val sender: DataEntryCommandSender,
  private val requestService: RequestService
) {

  @EventListener(condition = "#execution != null && #execution.eventName.equals('end')")
  fun notifyState(execution: DelegateExecution) {

    val reader = reader(execution)

    when (execution.currentActivityId) {
      AUDIT_SUBMITTED -> {
        val id = reader.get(REQUEST_ID)
        val originator = reader.get(ORIGINATOR)
        val request: Request = requestService.getRequest(id, getRevision(execution))
        val revision = updateAndStoreNewRevision(execution)
        notifyChange(
          request = request,
          state = IN_PROGRESS.of("Submitted"),
          log = "New approval request submitted.",
          revision = revision,
          username = originator
        )
      }
      else -> Unit
    }
  }


  @EventListener(condition = "#task != null && #task.eventName != null && #task.eventName.equals('complete')")
  @Order(TaskEventCollectorService.ORDER - 11)
  fun notifyRequestStatusChange(task: DelegateTask) {

    val reader = reader(task)
    val revision = getRevision(task)
    val id: String = reader.get(REQUEST_ID)
    val request: Request = requestService.getRequest(id, revision)
    val user = task.assignee

    when (task.taskDefinitionKey) {
      APPROVE_REQUEST -> {
        val approvalDecision = reader.getOptional(APPROVE_DECISION).orElseThrow { IllegalStateException("Approval decision must be provided") }
        when (approvalDecision) {
          APPROVE -> {
            notifyChange(
              request = request,
              state = COMPLETED.of("Approved"),
              log = "Request successfully approved.",
              revision = updateAndStoreNewRevision(task),
              username = task.assignee
            )
          }
          REJECT -> {
            notifyChange(
              request = request,
              state = COMPLETED.of("Rejected"),
              log = "Request rejected by approver ($user).",
              revision = updateAndStoreNewRevision(task),
              username = task.assignee
            )
          }
          RETURN -> {
            notifyChange(
              request = request,
              state = IN_PROGRESS.of("Returned"),
              log = "Request returned to originator.",
              revision = updateAndStoreNewRevision(task),
              username = task.assignee
            )
          }
          else -> Unit
        }
      }
      AMEND_REQUEST -> {
        val amendAction = reader.getOptional(AMEND_ACTION).orElseThrow { IllegalStateException("Amend action must be provided") }
        when (amendAction) {
          RESUBMIT -> {
            notifyChange(
              request = request,
              state = IN_PROGRESS.of("Resubmitted"),
              log = "Request resubmitted for approval.",
              revision = updateAndStoreNewRevision(task),
              username = task.assignee
            )
          }
          CANCEL -> {
            notifyChange(
              request = request,
              state = IN_PROGRESS.of("Cancelled"),
              log = "Request cancelled by originator.",
              revision = updateAndStoreNewRevision(task),
              username = task.assignee
            )
          }
          else -> Unit
        }

      }
      else -> Unit
    }

  }

  fun notifyChange(request: Request, state: DataEntryState, log: String, revision: Long, logNotes: String? = null, username: String? = null) {
    val authorizations: List<AuthorizationChange> = if (username != null) {
      listOf(addUser(username))
    } else {
      listOf()
    }
    sender.sendDataEntryChange(
      entryType = BusinessDataEntry.REQUEST,
      entryId = request.id,
      payload = request,
      state = state,
      name = "AR ${request.id}",
      description = request.subject,
      type = "Approval Request",
      modification = Modification(
        time = OffsetDateTime.now(),
        username = username,
        log = log,
        logNotes = logNotes
      ),
      authorizationChanges = authorizations,
      metaData = RevisionValue(revision).toMetaData()
    )
  }

  /**
   * Increments the revision in process variables and returns it.
   * @param task executed task
   * @return new revision number
   */
  private fun updateAndStoreNewRevision(variableScope: VariableScope): Long {
    // update the revision
    val oldRevision = getRevision(variableScope)
    val newRevision = oldRevision + 1
    PROJECTION_REVISION.on(variableScope).set(newRevision)
    return newRevision
  }

  private fun getRevision(variableScope: VariableScope): Long = PROJECTION_REVISION.from(variableScope).optional.orElseGet { 0L }

}


fun <T : Any> Optional<T>.isNotPresent(): Boolean = !this.isPresent
