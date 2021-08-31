package io.holunda.polyflow.example.process.approval.process

import io.holunda.camunda.bpm.data.CamundaBpmDataKotlin.customVariable
import io.holunda.camunda.bpm.data.CamundaBpmData.longVariable
import io.holunda.camunda.bpm.data.CamundaBpmData.stringVariable
import java.math.BigDecimal

object RequestApprovalProcess {
  const val KEY = "process_approve_request"
  const val RESOURCE = "process_approve_request.bpmn"

  object Variables {
    val REQUEST_ID = stringVariable("request")
    val ORIGINATOR = stringVariable("originator")
    val APPLICANT = stringVariable("applicant")
    val SUBJECT = stringVariable("subject")
    val AMOUNT = customVariable<BigDecimal>("amount")
    val CURRENCY = stringVariable("currency")

    val APPROVE_DECISION = stringVariable("approveDecision")
    val AMEND_ACTION = stringVariable("amendAction")
    val COMMENT = stringVariable("comment")

    val PROJECTION_REVISION = longVariable("projectionRevision")
  }

  object Elements {
    const val APPROVE_REQUEST = "user_approve_request"
    const val AMEND_REQUEST = "user_amend_request"

    const val AUDIT_SUBMITTED = "audit_submitted"
  }

  object Values {

    const val APPROVE = "APPROVE"
    const val REJECT = "REJECT"
    const val RETURN = "RETURN"
    const val CANCEL = "CANCEL"
    const val RESUBMIT = "RESUBMIT"

    val APPROVE_DECISION_VALUES = arrayOf(APPROVE, REJECT, RETURN)
    val AMEND_ACTION_VALUES = arrayOf(CANCEL, RESUBMIT)
  }
}


