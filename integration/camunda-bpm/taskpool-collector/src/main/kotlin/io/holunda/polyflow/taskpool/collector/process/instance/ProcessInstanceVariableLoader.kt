package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.taskpool.filterKeys
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

private val logger = KotlinLogging.logger {}

/**
 * Loads variables for running and already-ended process instances.
 */
class ProcessInstanceVariableLoader(
  private val runtimeService: RuntimeService,
  private val historyService: HistoryService,
) {
  /**
   * Retrieves all typed variables available for a lifecycle command.
   *
   * Runtime variables are preferred. When an end event has already removed the runtime instance, the loader
   * falls back to Camunda's historic variable instances.
   *
   * @param command lifecycle command providing the process instance id.
   * @param deserializeValues whether complex variable values should be deserialized.
   * @return variables available at the time of enrichment.
   */
  fun <T : ProcessInstanceCommand> getTypedVariables(command: T, deserializeValues: Boolean = true): VariableMap =
    try {
      runtimeService.getVariablesTyped(command.processInstanceId, deserializeValues)
    } catch (_: ProcessEngineException) {
      historicVariables(command)
    }

  /**
   * Retrieves selected typed variables available for a lifecycle command.
   *
   * @param command lifecycle command providing the process instance id.
   * @param variableNames names of variables to retrieve.
   * @param deserializeValues whether complex variable values should be deserialized.
   * @return selected variables available at the time of enrichment.
   */
  fun <T : ProcessInstanceCommand> getTypedVariables(
    command: T,
    variableNames: Collection<String>,
    deserializeValues: Boolean = true,
  ): VariableMap {
    if (variableNames.isEmpty()) return Variables.createVariables()
    return try {
      runtimeService.getVariablesTyped(command.processInstanceId, variableNames, deserializeValues)
    } catch (_: ProcessEngineException) {
      historicVariables(command).filterKeys { it in variableNames }
    }
  }

  private fun <T : ProcessInstanceCommand> historicVariables(command: T): VariableMap =
    Variables.createVariables().also { result ->
      historyService.createHistoricVariableInstanceQuery().processInstanceId(command.processInstanceId).list().forEach { variable ->
        // Camunda's historic variable API does not expose a non-deserializing variant.
        result.putValueTyped(variable.name, variable.typedValue)
      }
      if (result.isEmpty()) {
        logger.debug { "ENRICHER-005: Could not enrich variables for ended process instance ${command.processInstanceId}; no historic variables are available." }
      }
    }
}
