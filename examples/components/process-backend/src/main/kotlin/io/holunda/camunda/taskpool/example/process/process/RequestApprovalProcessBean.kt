package io.holunda.camunda.taskpool.example.process.process

import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Values.RESUBMIT
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.example.process.service.RequestService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.variable.Variables
import org.springframework.stereotype.Component

@Component
class RequestApprovalProcessBean(
  private val runtimeService: RuntimeService,
  private val taskService: TaskService,
  private val requestService: RequestService
) {

  /**
   * Starts the process for a given request id.
   */
  fun startProcess(requestId: String, originator: String, revision: Long = 1L): String {

    runtimeService.startProcessInstanceByKey(RequestApprovalProcess.KEY,
      requestId,
      Variables.createVariables()
        .putValue(RequestApprovalProcess.Variables.REQUEST_ID, requestId)
        .putValue(RequestApprovalProcess.Variables.ORIGINATOR, originator)
        .putValue(RequestApprovalProcess.Variables.PROJECTION_REVISION, revision)
    )
    requestService.changeRequestState(requestId, ProcessingType.IN_PROGRESS.of("Submitted"), originator, revision,"New approval request submitted.")
    return requestId
  }

  /**
   * Completes the approval process if located in approve request task.
   */
  fun approveProcess(processInstanceId: String, decision: String, username: String, comment: String?) {
    if (!RequestApprovalProcess.Values.APPROVE_DECISION.contains(decision.toUpperCase())) {
      throw IllegalArgumentException("Only one of APPROVE, RETURN, REJECT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .processInstanceBusinessKey(processInstanceId)
      .taskDefinitionKey(RequestApprovalProcess.Elements.APPROVE_REQUEST)
      .singleResult()
    taskService.claim(task.id, username)

    taskService.complete(task.id,
      Variables
        .createVariables()
        .putValue(RequestApprovalProcess.Variables.APPROVE_DECISION, Variables.stringValue(decision.toUpperCase()))
        .putValue(RequestApprovalProcess.Variables.COMMENT, Variables.stringValue(comment))
    )
  }

  /**
   * Completes the approval process if located in amend request task.
   */
  fun amendProcess(id: String, action: String, username: String, comment: String?) {

    if (!RequestApprovalProcess.Values.AMEND_ACTION.contains(action.toUpperCase())) {
      throw IllegalArgumentException("Only one of CANCEL, RESUBMIT is supported.")
    }

    val task = taskService.createTaskQuery()
      .processInstanceBusinessKey(id)
      .taskDefinitionKey(RequestApprovalProcess.Elements.AMEND_REQUEST)
      .singleResult()

    taskService.claim(task.id, username)

    taskService.complete(task.id, Variables.createVariables()
      .putValue(RequestApprovalProcess.Variables.AMEND_ACTION, Variables.stringValue(action.toUpperCase()))
      .putValue(RequestApprovalProcess.Variables.COMMENT, Variables.stringValue(comment))
    )
  }

  /**
   * Completes the approve request task with given id, decision and optional comment.
   */
  fun approveTask(taskId: String, decision: String, username: String, comment: String?) {
    if (!RequestApprovalProcess.Values.APPROVE_DECISION.contains(decision.toUpperCase())) {
      throw IllegalArgumentException("Only one of APPROVE, RETURN, REJECT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .taskId(taskId)
      .taskDefinitionKey(RequestApprovalProcess.Elements.APPROVE_REQUEST)
      .singleResult() ?: throw NoSuchElementException("Task with id $taskId not found.")

    val requestId = runtimeService.getVariable(task.executionId, RequestApprovalProcess.Variables.REQUEST_ID) as String

    val newRevision = updateAndStoreNewRevision(task)

    requestService.changeRequestState(
      id = requestId,
      username = username,
      revision = newRevision,
      state = ProcessingType.IN_PROGRESS.of("Decided"),
      log = "Approval decision was $decision.",
      logNotes = comment
    )

    taskService.claim(task.id, username)

    taskService.complete(task.id,
      Variables
        .createVariables()
        .putValue(RequestApprovalProcess.Variables.APPROVE_DECISION, Variables.stringValue(decision.toUpperCase()))
        .putValue(RequestApprovalProcess.Variables.COMMENT, Variables.stringValue(comment))
    )
  }

  /**
   * Completes the amend request task with given id, action and optional comment.
   */
  fun amendTask(taskId: String, action: String, request: Request, username: String, comment: String?) {
    if (!RequestApprovalProcess.Values.AMEND_ACTION.contains(action.toUpperCase())) {
      throw IllegalArgumentException("Only one of CANCEL, RESUBMIT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .taskId(taskId)
      .taskDefinitionKey(RequestApprovalProcess.Elements.AMEND_REQUEST)
      .singleResult() ?: throw NoSuchElementException("Task with id $taskId not found.")

    if (task.assignee != null) {
      taskService.claim(task.id, username)
    }

    if (action == RESUBMIT) {
      if (requestService.checkRequest(request.id)) {
        val newRevision = updateAndStoreNewRevision(task)
        requestService.updateRequest(id = request.id, request = request, username = username, revision = newRevision)
      } else {
        throw IllegalArgumentException("Request with id ${request.id} was not found.")
      }
    }

    taskService.complete(task.id,
      Variables
        .createVariables()
        .putValue(RequestApprovalProcess.Variables.AMEND_ACTION, Variables.stringValue(action.toUpperCase()))
        .putValue(RequestApprovalProcess.Variables.COMMENT, Variables.stringValue(comment))
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
   * Retrieve the number of running instances.
   */
  fun countInstances() = getAllInstancesQuery().active().count()

  /**
   * Loads approve task form data.
   */
  fun loadApproveTaskFormData(id: String): TaskAndRequest {
    val task = taskService.createTaskQuery()
      .taskId(id)
      .taskDefinitionKey(RequestApprovalProcess.Elements.APPROVE_REQUEST)
      .initializeFormKeys()
      .singleResult() ?: throw NoSuchElementException("Task with id $id not found.")

    val requestId = (this.runtimeService.getVariable(task.executionId, RequestApprovalProcess.Variables.REQUEST_ID)
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
      .taskDefinitionKey(RequestApprovalProcess.Elements.AMEND_REQUEST)
      .initializeFormKeys()
      .singleResult() ?: throw NoSuchElementException("Task with id $id not found.")

    val requestId = (this.runtimeService.getVariable(task.executionId, RequestApprovalProcess.Variables.REQUEST_ID)
      ?: throw NoSuchElementException("Request id could not be found for task $id")) as String
    val request = this.requestService.getRequest(requestId)
    return TaskAndRequest(task = task, approvalRequest = request)
  }

  /**
   * Retrieves all running instances.
   */
  private fun getAllInstancesQuery() =
    runtimeService
      .createProcessInstanceQuery()
      .processDefinitionKey(RequestApprovalProcess.KEY)

  /**
   * Increments the revision in process variables and returns it.
   * @param task executed task
   * @return new revision number
   */
  private fun updateAndStoreNewRevision(task: Task): Long {
    // update the revision
    val oldRevision = (runtimeService.getVariable(task.executionId, RequestApprovalProcess.Variables.PROJECTION_REVISION)?:0L) as Long
    val newRevision = oldRevision + 1
    runtimeService.setVariable(task.executionId, RequestApprovalProcess.Variables.PROJECTION_REVISION, newRevision)
    return newRevision
  }

}

data class TaskAndRequest(val task: Task, val approvalRequest: Request)
