package io.holunda.polyflow.example.process.approval.process.delegate

import io.holunda.camunda.bpm.data.CamundaBpmData.writer
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.AMEND_ACTION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.AMOUNT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.APPLICANT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.APPROVE_DECISION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.CURRENCY
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.PROJECTION_REVISION
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.REQUEST_ID
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.SUBJECT
import io.holunda.polyflow.example.process.approval.service.RequestService
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


