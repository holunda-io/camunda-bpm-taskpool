package io.holunda.camunda.taskpool.example.process.process.delegate

import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess
import io.holunda.camunda.taskpool.example.process.service.RequestService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class LoadRequestDelegate(
  private val requestService: RequestService
) : JavaDelegate {

  override fun execute(execution: DelegateExecution) {

    val id: String = execution.getVariable(RequestApprovalProcess.Variables.REQUEST_ID) as String
    val request = requestService.getRequest(id)

    execution.setVariable(RequestApprovalProcess.Variables.APPLICANT, request.applicant)
    execution.setVariable(RequestApprovalProcess.Variables.AMOUNT, request.amount)
    execution.setVariable(RequestApprovalProcess.Variables.CURRENCY, request.currency)
    execution.setVariable(RequestApprovalProcess.Variables.SUBJECT, request.subject)

    execution.setVariable(RequestApprovalProcess.Variables.APPROVE_DECISION, "")
    execution.setVariable(RequestApprovalProcess.Variables.AMEND_ACTION, "")
  }

}


