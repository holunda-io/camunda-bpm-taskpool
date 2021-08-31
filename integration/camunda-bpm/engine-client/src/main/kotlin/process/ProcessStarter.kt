package io.holunda.polyflow.client.camunda.process

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.VariableMap
import org.springframework.stereotype.Component

/**
 * Starts process.
 */
@Component
class ProcessStarter(private val runtimeService: RuntimeService) {

  /**
   * Starts process.
   * @param processDefinitionKey definition key.
   * @param payload variables.
   * @param businessKey optional business key.
   * @return process instance id.
   */
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
