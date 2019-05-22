package io.holunda.camunda.taskpool.example.process.process

import io.holunda.camunda.datapool.sender.DataEntryCommandSender
import io.holunda.camunda.taskpool.example.process.service.RequestService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.springframework.stereotype.Component

@Component
class RequestStatusListener(
  private val sender: DataEntryCommandSender,
  private val requestService: RequestService
) : ExecutionListener {

  override fun notify(execution: DelegateExecution) {
    val id: String = execution.getVariable(ProcessApproveRequest.Variables.REQUEST_ID) as String
    // FIXME: IMPLEMENT ME
  }
}
