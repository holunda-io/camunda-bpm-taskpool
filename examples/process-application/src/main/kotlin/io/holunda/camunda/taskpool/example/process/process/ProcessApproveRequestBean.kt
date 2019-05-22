package io.holunda.camunda.taskpool.example.process.process

import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.example.process.service.RequestService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.variable.Variables
import org.springframework.stereotype.Component

@Component
class ProcessApproveRequestBean(
  private val runtimeService: RuntimeService,
  private val taskService: TaskService,
  private val requestService: RequestService
) {

  /**
   * Starts the process for a given request id.
   */
  fun startProcess(requestId: String, originator: String = "kermit"): String {

    runtimeService.startProcessInstanceByKey(ProcessApproveRequest.KEY,
      requestId,
      Variables.createVariables()
        .putValue(ProcessApproveRequest.Variables.REQUEST_ID, requestId)
        .putValue(ProcessApproveRequest.Variables.ORIGINATOR, originator)
    )
    return requestId
  }

  /**
   * Completes the approval process if located in approve request task.
   */
  fun approveProcess(processInstanceId: String, decision: String, comment: String?) {
    if (!ProcessApproveRequest.Values.APPROVE_DECISION.contains(decision.toUpperCase())) {
      throw IllegalArgumentException("Only one asState APPROVE, RETURN, REJECT is supported.")
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
        .putValue(ProcessApproveRequest.Variables.APPROVE_DECISION, Variables.stringValue(decision.toUpperCase()))
        .putValue(ProcessApproveRequest.Variables.COMMENT, Variables.stringValue(comment))
    )
  }

  /**
   * Completes the approval process if located in amend request task.
   */
  fun amendProcess(id: String, action: String, comment: String?) {

    if (!ProcessApproveRequest.Values.AMEND_ACTION.contains(action.toUpperCase())) {
      throw IllegalArgumentException("Only one asState CANCEL, RESUBMIT is supported.")
    }

    val task = taskService.createTaskQuery()
      .processInstanceBusinessKey(id)
      .taskDefinitionKey(ProcessApproveRequest.Elements.AMEND_REQUEST)
      .singleResult()

    taskService.complete(task.id, Variables.createVariables()
      .putValue(ProcessApproveRequest.Variables.AMEND_ACTION, Variables.stringValue(action.toUpperCase()))
      .putValue(ProcessApproveRequest.Variables.COMMENT, Variables.stringValue(comment))
    )
  }

  /**
   * Completes the approve request task with given id, decision and optional comment.
   */
  fun approveTask(taskId: String, decision: String, comment: String?) {
    if (!ProcessApproveRequest.Values.APPROVE_DECISION.contains(decision.toUpperCase())) {
      throw IllegalArgumentException("Only one asState APPROVE, RETURN, REJECT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .taskId(taskId)
      .taskDefinitionKey(ProcessApproveRequest.Elements.APPROVE_REQUEST)
      .singleResult() ?: throw NoSuchElementException("Task with id $taskId not found.")
    val requestId = runtimeService.getVariable(task.executionId, ProcessApproveRequest.Variables.REQUEST_ID) as String
    taskService.complete(task.id,
      Variables
        .createVariables()
        .putValue(ProcessApproveRequest.Variables.APPROVE_DECISION, Variables.stringValue(decision.toUpperCase()))
        .putValue(ProcessApproveRequest.Variables.COMMENT, Variables.stringValue(comment))
    )

    val stateWithLog: Pair<String, String> = when (decision) {
      "APPROVE" -> "Approved" to "Request approved."
      "REJECT" -> "Rejected" to "Request rejected."
      "RETURN" -> "Returned" to "Request returned to originator."
      else -> "" to ""
    }


    requestService.changeRequestState(
      id = requestId,
      state = ProcessingType.IN_PROGRESS.asState(stateWithLog.first),
      username = null, // FIXME
      log = stateWithLog.second,
      logNotes = comment
    )

  }

  /**
   * Completes the amend request task with given id, action and optional comment.
   */
  fun amendTask(taskId: String, action: String, request: Request, comment: String?) {
    if (!ProcessApproveRequest.Values.AMEND_ACTION.contains(action.toUpperCase())) {
      throw IllegalArgumentException("Only one asState CANCEL, RESUBMIT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .taskId(taskId)
      .taskDefinitionKey(ProcessApproveRequest.Elements.AMEND_REQUEST)
      .singleResult() ?: throw NoSuchElementException("Task with id $taskId not found.")

    if (action == "RESUBMIT") {
      if (requestService.checkRequest(request.id)) {
        requestService.updateRequest(id = request.id, request = request, username = null)
      } else {
        throw IllegalArgumentException("Request with id ${request.id} was not found.")
      }
    }

    taskService.complete(task.id,
      Variables
        .createVariables()
        .putValue(ProcessApproveRequest.Variables.AMEND_ACTION, Variables.stringValue(action.toUpperCase()))
        .putValue(ProcessApproveRequest.Variables.COMMENT, Variables.stringValue(comment))
    )
  }

  /**
   * Deletes all instances.
   */
  fun deleteAllInstances() {
    getAllInstancesQuery()
      .list()
      .forEach {
        runtimeService
          .deleteProcessInstance(it.processInstanceId, "Deleted by the mass deletion REST call")
      }
  }

  /**
   * Retrieve the number asState running instances.
   */
  fun countInstances() = getAllInstancesQuery().active().count()

  /**
   * Retrieves all running instances.
   */
  private fun getAllInstancesQuery() =
    runtimeService
      .createProcessInstanceQuery()
      .processDefinitionKey(ProcessApproveRequest.KEY)

  /**
   * Loads approve task form data.
   */
  fun loadApproveTaskFormData(id: String): TaskAndRequest {
    val task = taskService.createTaskQuery()
      .taskId(id)
      .taskDefinitionKey(ProcessApproveRequest.Elements.APPROVE_REQUEST)
      .initializeFormKeys()
      .singleResult() ?: throw NoSuchElementException("Task with id $id not found.")

    val requestId = (this.runtimeService.getVariable(task.executionId, ProcessApproveRequest.Variables.REQUEST_ID)
      ?: throw NoSuchElementException("Request id could not be found for task $id")) as String
    val request = this.requestService.getRequest(requestId)
    return TaskAndRequest(task = task, approvalRequest = request)
  }

  /**
   * Loads amend task form data.
   */
  fun loadAmendTaskFormData(id: String): TaskAndRequest {
    val task = taskService.createTaskQuery()
      .taskId(id)
      .taskDefinitionKey(ProcessApproveRequest.Elements.AMEND_REQUEST)
      .initializeFormKeys()
      .singleResult() ?: throw NoSuchElementException("Task with id $id not found.")

    val requestId = (this.runtimeService.getVariable(task.executionId, ProcessApproveRequest.Variables.REQUEST_ID)
      ?: throw NoSuchElementException("Request id could not be found for task $id")) as String
    val request = this.requestService.getRequest(requestId)
    return TaskAndRequest(task = task, approvalRequest = request)
  }

}

data class TaskAndRequest(val task: Task, val approvalRequest: Request)
