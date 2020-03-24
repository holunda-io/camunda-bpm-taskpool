package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.query.process.ProcessDefinitionsStartableByUserQuery

/**
 * Queries for process definitions.
 */
interface ProcessDefinitionApi {

  /**
   * Query for process definitions startable by user.
   * @param query query object.
   * @return list of process definitions.
   */
  fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition>
}
