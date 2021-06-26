package io.holunda.polyflow.view.query.process.variable

import org.axonframework.queryhandling.QueryResponseMessage

/**
 * Process variable API.
 */
interface ProcessVariableApi {

  /**
   * Query for process variables.
   */
  fun query(query: ProcessVariablesForInstanceQuery): QueryResponseMessage<ProcessVariableQueryResult>
}
