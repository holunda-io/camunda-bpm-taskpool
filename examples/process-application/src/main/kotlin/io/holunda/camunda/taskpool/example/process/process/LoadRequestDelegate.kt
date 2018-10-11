package io.holunda.camunda.taskpool.example.process.process

import io.holunda.camunda.taskpool.example.process.service.RequestService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class LoadRequestDelegate(private val requestService: RequestService) : JavaDelegate {

  override fun execute(execution: DelegateExecution) {

    val id: String = execution.getVariable(ProcessApproveRequest.Variables.REQUEST_ID) as String
    val request = requestService.getRequest(id)
      ?: throw NoSuchElementException("Request with id $id could not found.")

    execution.setVariable(ProcessApproveRequest.Variables.ORIGINATOR, request.originator)
    execution.setVariable(ProcessApproveRequest.Variables.APPLICANT, request.applicant)
    execution.setVariable(ProcessApproveRequest.Variables.AMOUNT, request.amount)
    execution.setVariable(ProcessApproveRequest.Variables.CURRENCY, request.currency)
    execution.setVariable(ProcessApproveRequest.Variables.SUBJECT, request.subject)
  }

}


