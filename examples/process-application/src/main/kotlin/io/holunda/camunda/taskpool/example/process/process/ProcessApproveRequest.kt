package io.holunda.camunda.taskpool.example.process.process

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Variables.ORIGINATOR
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest.Variables.REQUEST_ID
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.springframework.stereotype.Component

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
  private val taskService: TaskService
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

  fun approve(id: String, decision: String, comment: String?) {

    if (!arrayOf("APPROVE", "RETURN", "REJECT").contains(decision.toUpperCase())) {
      throw IllegalArgumentException("Only one of APPROVE, RETURN, REJECT is supported.")
    }

    val task = taskService
      .createTaskQuery()
      .processInstanceBusinessKey(id)
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

  fun amend(id: String, action: String) {

    if (!arrayOf("CANCEL", "RESUBMIT").contains(action.toUpperCase())) {
      throw IllegalArgumentException("Only one of CANCEL, RESUBMIT is supported.")
    }


    val task = taskService.createTaskQuery()
      .processInstanceBusinessKey(id)
      .taskDefinitionKey(ProcessApproveRequest.Elements.AMEND_REQUEST)
      .singleResult()
    taskService.complete(task.id, Variables.createVariables().putValue(ProcessApproveRequest.Variables.AMEND_ACTION, stringValue(action.toUpperCase())))
  }


  fun countInstances() = getAllInstancesQuery().count()

  fun deleteAllInstances() {
    getAllInstancesQuery().list().forEach { runtimeService.deleteProcessInstance(it.processInstanceId, "Deleted by the mass deletion REST call") }
  }

  private fun getAllInstancesQuery() = runtimeService.createProcessInstanceQuery().processDefinitionKey(ProcessApproveRequest.KEY)
}

