package io.holunda.polyflow.client.processengineapi.process

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd
import org.springframework.stereotype.Component

/**
 * Starts process.
 */
@Component
class ProcessStarter(private val startProcessApi: StartProcessApi) {

  /**
   * Starts process.
   * @param processDefinitionKey definition key.
   * @param payload variables.
   * @param businessKey optional business key.
   * @return process instance id.
   */
  fun startProcess(
    processDefinitionKey: String,
    payload: Map<String, Object>,
    businessKey: String?
  ): String {

    val restrictions = mutableMapOf<String, String>()
    businessKey?.let { restrictions[CommonRestrictions.BUSINESS_KEY] = it }

    val startProcess = startProcessApi.startProcess(
      StartProcessByDefinitionCmd(
        definitionKey = processDefinitionKey,
        payload = payload,
        restrictions = restrictions
      )
    )
    
    return startProcess.get().instanceId
  }
}
