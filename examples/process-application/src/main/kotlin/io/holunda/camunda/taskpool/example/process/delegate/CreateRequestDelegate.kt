package io.holunda.camunda.taskpool.example.process.delegate

import io.holunda.camunda.taskpool.api.business.CreateDataEntryCommand
import io.holunda.camunda.taskpool.sender.DataEntryCommandSender
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class CreateRequestDelegate(private val sender: DataEntryCommandSender): JavaDelegate {

  override fun execute(execution: DelegateExecution) {

    sender.sendDataEntryCommand(CreateDataEntryCommand(
      entryType = "Request",
      entryId = execution.businessKey,
      payload = execution.variablesTyped
    ))
  }

}
