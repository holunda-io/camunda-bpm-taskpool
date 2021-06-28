package io.holunda.polyflow.view.query.process

import io.holunda.polyflow.view.ProcessDefinition

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
