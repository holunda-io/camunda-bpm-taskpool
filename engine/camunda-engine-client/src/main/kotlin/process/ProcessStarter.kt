package io.holunda.camunda.client.process

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.VariableMap
import org.springframework.stereotype.Component

@Component
class ProcessStarter(private val runtimeService: RuntimeService) {

  fun startProcess(
    processDefinitionKey: String,
    payload: VariableMap,
    businessKey: String?
  ): String {
    val instance = if (businessKey != null) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, payload)
    } else {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, payload)
    }

    return instance.processInstanceId

  }
}
