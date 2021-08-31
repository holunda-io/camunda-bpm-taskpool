package io.holunda.polyflow.example.process.approval.process

import io.holunda.camunda.bpm.data.CamundaBpmData.builder
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Values.RESUBMIT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.AMEND_ACTION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.APPROVE_DECISION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.COMMENT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.ORIGINATOR
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.PROJECTION_REVISION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.REQUEST_ID
import io.holunda.polyflow.example.process.approval.service.Request
import io.holunda.polyflow.example.process.approval.service.RequestService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.NoSuchElementException

@Component
@Transactional
class RequestApprovalProcessBean(
  private val runtimeService: RuntimeService,
  private val taskService: TaskService,
  private val requestService: RequestService
) {

  /**
   * Creates draft.
   * @param request request draft to save.
   * @param originator user saved the request.
   * @param revision revision of te command.
   */
  fun submitDraft(request: Request, originator: String, revision: Long = 1L) {
    requestService.addRequest(request, originator, revision)
    startProcess(request.id, originator, revision)
  }

  /**
   * Starts the process for a given request id.
   */
  fun startProcess(requestId: String, originator: String, revision: Long = 1L): String {

    runtimeService.startProcessInstanceByKey(RequestApprovalProcess.KEY,
      requestId,
      builder()
        .set(REQUEST_ID, requestId)
        .set(ORIGINATOR, originator)
        .set(PROJECTION_REVISION, revision)
        .build()
    )
    return requestId
  }

  /**
   * Completes the approve request task with given id, decision and optional comment.
   */
  fun approveTask(taskId: String, decision: String, username: String, comment: String?) {
    if (!RequestApprovalProcess.Values.APPROVE_DECISION_VALUES.contains(decision.uppercase())) {
      throw IllegalArgumentException("Only one of APPROVE, RETURN, REJECT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .taskId(taskId)
      .taskDefinitionKey(RequestApprovalProcess.Elements.APPROVE_REQUEST)
      .singleResult() ?: throw NoSuchElementException("Task with id $taskId not found.")

    taskService.claim(task.id, username)

    taskService.complete(task.id,
      builder()
        .set(APPROVE_DECISION, decision.uppercase(Locale.getDefault()))
        .set(COMMENT, comment)
        .build()
    )
  }

  /**
   * Completes the amend request task with given id, action and optional comment.
   */
  fun amendTask(taskId: String, action: String, request: Request, username: String, comment: String?) {
    if (!RequestApprovalProcess.Values.AMEND_ACTION_VALUES.contains(action.uppercase())) {
      throw IllegalArgumentException("Only one of CANCEL, RESUBMIT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .taskId(taskId)
      .taskDefinitionKey(RequestApprovalProcess.Elements.AMEND_REQUEST)
      .singleResult() ?: throw NoSuchElementException("Task with id $taskId not found.")

    val revision = PROJECTION_REVISION.from(taskService, task.id).get() + 1

    if (task.assignee != null) {
      // un-claim the task
      if (task.assignee != username) {
        task.assignee = null
        taskService.saveTask(task)
      }
      taskService.claim(task.id, username)
    }

    val variables = builder()
      .set(AMEND_ACTION, action.uppercase(Locale.getDefault()))
      .set(COMMENT, comment)


    if (action == RESUBMIT) {
      val updatedRevision = requestService.updateRequest(id = request.id, request = request, username = username, revision = revision)
      variables
        .set(PROJECTION_REVISION, updatedRevision)
    }
    taskService.complete(task.id, variables.build())
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

    val requestId = REQUEST_ID.from(runtimeService, task.executionId).optional.orElseThrow { NoSuchElementException("Request id could not be found for task $id") }
    val revision = PROJECTION_REVISION.from(runtimeService, task.executionId).optional.orElseThrow { NoSuchElementException("Project revision could not be found for task $id") }

    val request = this.requestService.getRequest(requestId, revision)
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

    val requestId = REQUEST_ID.from(runtimeService, task.executionId).optional.orElseThrow { NoSuchElementException("Request id could not be found for task $id") }
    val revision = PROJECTION_REVISION.from(runtimeService, task.executionId).optional.orElseThrow { NoSuchElementException("Project revision could not be found for task $id") }

    val request = this.requestService.getRequest(requestId, revision)
    return TaskAndRequest(task = task, approvalRequest = request)
  }

  /**
   * Retrieves all running instances.
   */
  private fun getAllInstancesQuery() =
    runtimeService
      .createProcessInstanceQuery()
      .processDefinitionKey(RequestApprovalProcess.KEY)

}

data class TaskAndRequest(val task: Task, val approvalRequest: Request)
