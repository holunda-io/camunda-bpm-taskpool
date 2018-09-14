package io.holunda.camunda.taskpool.example.process.process

import io.holunda.camunda.taskpool.api.business.CreateDataEntryCommand
import io.holunda.camunda.taskpool.sender.DataEntryCommandSender
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class StoreRequestDelegate(private val sender: DataEntryCommandSender): JavaDelegate {

  override fun execute(execution: DelegateExecution) {
    // FIXME allow for multiple updates!
    /*
    sender.sendDataEntryCommand(CreateDataEntryCommand(
      entryType = "Request",
      entryId = execution.businessKey,
      payload = execution.variablesTyped
    ))
    */
  }

}
