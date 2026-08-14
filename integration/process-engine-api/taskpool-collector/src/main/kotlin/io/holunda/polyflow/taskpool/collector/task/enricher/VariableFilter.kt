package io.holunda.polyflow.taskpool.collector.task.enricher

/**
 * To be implemented by classes that filter process variables. Used during enrichment to decide which process variables are added to a task's payload.
 */
interface VariableFilter {

  val processDefinitionKey: ProcessDefinitionKey?

  /**
   * Returns whether the process variable with the given name shall be contained in the payload of the given task.
   * @param taskDefinitionKey the key of the task to be enriched
   * @param variableName the name of the process variable
   */
  fun filter(taskDefinitionKey: TaskDefinitionKey, variableName: VariableName): Boolean

}
