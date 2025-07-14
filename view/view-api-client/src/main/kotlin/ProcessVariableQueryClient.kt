package io.holunda.polyflow.view

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holunda.polyflow.view.query.process.variable.ProcessVariableQueryResult
import io.holunda.polyflow.view.query.process.variable.ProcessVariablesForInstanceQuery
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture

/**
 * Client encapsulating the correct query types (including response types)
 */
open class ProcessVariableQueryClient(
  private val queryGateway: QueryGateway
) {

  /**
   * @see io.holunda.polyflow.view.query.process.ProcessInstanceApi.query
   * @see io.holunda.polyflow.view.query.process.variable.ProcessVariablesForInstanceQuery
   */
  fun query(query: ProcessVariablesForInstanceQuery): CompletableFuture<ProcessVariableQueryResult> =
    queryGateway.query(
      query,
      QueryResponseMessageResponseType.queryResponseMessageResponseType<ProcessVariableQueryResult>()
    )

}
