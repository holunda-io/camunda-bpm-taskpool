package io.holunda.polyflow.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand

/**
 * Leaves process-definition commands unchanged.
 */
class EmptyProcessDefinitionCommandEnricher : ProcessDefinitionCommandEnricher {

  /**
   * Returns the command without adding payload values.
   *
   * @param command command to leave unchanged.
   * @return the original command.
   */
  override fun <T : ProcessDefinitionCommand> enrich(command: T): T = command
}
