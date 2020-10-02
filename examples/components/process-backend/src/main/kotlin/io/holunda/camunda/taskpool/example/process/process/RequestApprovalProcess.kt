package io.holunda.camunda.taskpool.example.process.process

object RequestApprovalProcess {
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

    const val PROJECTION_REVISION = "projectionRevision"
  }

  object Elements {
    const val APPROVE_REQUEST = "user_approve_request"
    const val AMEND_REQUEST = "user_amend_request"
  }

  object Values {

    const val APPROVE = "APPROVE"
    const val REJECT = "REJECT"
    const val RETURN = "RETURN"
    const val CANCEL = "CANCEL"
    const val RESUBMIT = "RESUBMIT"

    val APPROVE_DECISION = arrayOf(APPROVE, REJECT, RETURN)
    val AMEND_ACTION = arrayOf(CANCEL, RESUBMIT)
  }
}


