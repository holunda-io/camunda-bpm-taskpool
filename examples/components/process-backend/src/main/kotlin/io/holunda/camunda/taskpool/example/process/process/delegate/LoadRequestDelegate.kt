package io.holunda.camunda.taskpool.example.process.process.delegate

import io.holunda.camunda.bpm.data.CamundaBpmData.writer
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Variables.AMEND_ACTION
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Variables.AMOUNT
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Variables.APPLICANT
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Variables.APPROVE_DECISION
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Variables.CURRENCY
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Variables.PROJECTION_REVISION
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Variables.REQUEST_ID
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcess.Variables.SUBJECT
import io.holunda.camunda.taskpool.example.process.service.RequestService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class LoadRequestDelegate(
  private val requestService: RequestService
) : JavaDelegate {

  override fun execute(execution: DelegateExecution) {

    val id: String = REQUEST_ID.from(execution).get()
    val revision: Long = PROJECTION_REVISION.from(execution).get()
    val request = requestService.getRequest(id, revision)

    writer(execution)
      .set(APPLICANT, request.applicant)
      .set(AMOUNT, request.amount)
      .set(CURRENCY, request.currency)
      .set(SUBJECT, request.subject)
      .set(APPROVE_DECISION, "")
      .set(AMEND_ACTION, "")
  }

}


