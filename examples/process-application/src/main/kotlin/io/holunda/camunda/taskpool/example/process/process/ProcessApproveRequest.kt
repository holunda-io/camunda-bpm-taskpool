package io.holunda.camunda.taskpool.example.process.process

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Variables.ORIGINATOR
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Variables.REQUEST_ID
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDto
import io.holunda.camunda.taskpool.example.process.rest.model.TaskDto
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.example.process.service.RequestService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneId

object ProcessApproveRequest {
  const val KEY = "process_approve_request"
  const val RESOURCE = "process_approve_request.bpmn"

  object Variables {
    const val REQUEST_ID = "request"
    const val ORIGINATOR = "originator"
    const val APPLICANT = "applicant"
    const val SUBJECT = "subject"
    const val AMOUNT = "amount"
    const val CURRENCY = "currency"

    const val APPROVE_DECISION = "approveDecision"
    const val AMEND_ACTION = "amendAction"
    const val COMMENT = "comment"

  }

  object Elements {
    const val APPROVE_REQUEST = "user_approve_request"
    const val AMEND_REQUEST = "user_amend_request"
  }
}

@Component
class ProcessApproveRequestBean(
  private val runtimeService: RuntimeService,
  private val taskService: TaskService,
  private val requestService: RequestService
) {

  fun startProcess(requestId: String): String {
    runtimeService.startProcessInstanceByKey(ProcessApproveRequest.KEY,
      requestId,
      Variables.createVariables()
        .putValue(REQUEST_ID, requestId)
        .putValue(ORIGINATOR, "kermit")
    )
    return requestId
  }

  fun approveProcess(processInstanceId: String, decision: String, comment: String?) {

    if (!arrayOf("APPROVE", "RETURN", "REJECT").contains(decision.toUpperCase())) {
      throw IllegalArgumentException("Only one of APPROVE, RETURN, REJECT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .processInstanceBusinessKey(processInstanceId)
      .taskDefinitionKey(ProcessApproveRequest.Elements.APPROVE_REQUEST)
      .singleResult()
    taskService.claim(task.id, "gonzo")
    taskService.complete(task.id,
      Variables
        .createVariables()
        .putValue(ProcessApproveRequest.Variables.APPROVE_DECISION, stringValue(decision.toUpperCase()))
        .putValue(ProcessApproveRequest.Variables.COMMENT, stringValue(comment))
    )
  }

  fun amendProcess(id: String, action: String) {

    if (!arrayOf("CANCEL", "RESUBMIT").contains(action.toUpperCase())) {
      throw IllegalArgumentException("Only one of CANCEL, RESUBMIT is supported.")
    }


    val task = taskService.createTaskQuery()
      .processInstanceBusinessKey(id)
      .taskDefinitionKey(ProcessApproveRequest.Elements.AMEND_REQUEST)
      .singleResult()
    taskService.complete(task.id, Variables.createVariables()
      .putValue(ProcessApproveRequest.Variables.AMEND_ACTION, stringValue(action.toUpperCase()))
    )
  }

  fun approveTask(taskId: String, decision: String, comment: String?) {
    if (!arrayOf("APPROVE", "RETURN", "REJECT").contains(decision.toUpperCase())) {
      throw IllegalArgumentException("Only one of APPROVE, RETURN, REJECT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .taskId(taskId)
      .taskDefinitionKey(ProcessApproveRequest.Elements.APPROVE_REQUEST)
      .singleResult()
    taskService.complete(task.id,
      Variables
        .createVariables()
        .putValue(ProcessApproveRequest.Variables.APPROVE_DECISION, stringValue(decision.toUpperCase()))
        .putValue(ProcessApproveRequest.Variables.COMMENT, stringValue(comment))
    )
  }

  fun amendTask(taskId: String, action: String, comment: String?) {
    if (!arrayOf("CANCEL", "RESUBMIT").contains(action.toUpperCase())) {
      throw IllegalArgumentException("Only one of CANCEL, RESUBMIT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .taskId(taskId)
      .taskDefinitionKey(ProcessApproveRequest.Elements.AMEND_REQUEST)
      .singleResult()
    taskService.complete(task.id,
      Variables
        .createVariables()
        .putValue(ProcessApproveRequest.Variables.AMEND_ACTION, stringValue(action.toUpperCase()))
    )
  }


  fun countInstances() = getAllInstancesQuery().count()

  fun deleteAllInstances() {
    getAllInstancesQuery().list().forEach { runtimeService.deleteProcessInstance(it.processInstanceId, "Deleted by the mass deletion REST call") }
  }

  private fun getAllInstancesQuery() = runtimeService.createProcessInstanceQuery().processDefinitionKey(ProcessApproveRequest.KEY)

  fun loadApproveTaskFormData(id: String): ApproveTaskFormData {
    val task = taskService.createTaskQuery()
      .taskId(id)
      .taskDefinitionKey(ProcessApproveRequest.Elements.APPROVE_REQUEST)
      .initializeFormKeys()
      .singleResult() ?: throw NoSuchElementException("Task with id $id not found.")

    val requestId = (this.runtimeService.getVariable(task.executionId, ProcessApproveRequest.Variables.REQUEST_ID)
      ?: throw NoSuchElementException("Request id could not be found for task $id")) as String
    val request = this.requestService.getRequest(requestId)
      ?: throw NoSuchElementException("Request could not be found for request id $requestId")

    return ApproveTaskFormData(task = taskDto(task), approvalRequest = approvalRequestDto(request))
  }

  private fun approvalRequestDto(request: Request): ApprovalRequestDto = ApprovalRequestDto()
    .id(request.id)
    .amount(request.amount.toString())
    .applicant(request.applicant)
    .currency(request.currency)
    .subject(request.subject)

  private fun taskDto(task: Task): TaskDto = TaskDto()
    .id(task.id)
    .assignee(task.assignee)
    .createTime(OffsetDateTime.ofInstant(task.createTime.toInstant(), ZoneId.systemDefault()))
    .description(task.description)
    .dueDate(OffsetDateTime.ofInstant(task.dueDate.toInstant(), ZoneId.systemDefault()))
    .followUpDate(OffsetDateTime.ofInstant(task.followUpDate.toInstant(), ZoneId.systemDefault()))
    .formKey(task.formKey)
    .name(task.name)
    .priority(task.priority)
}

data class ApproveTaskFormData(val task: TaskDto, val approvalRequest: ApprovalRequestDto)

