package io.holunda.polyflow.view

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holunda.polyflow.view.query.process.ProcessInstanceQueryResult
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture

/**
 * Client encapsulating the correct query types (including response types)
 */
open class ProcessInstanceQueryClient(
  private val queryGateway: QueryGateway
) {

  /**
   * @see io.holunda.polyflow.view.query.process.ProcessInstanceApi.query
   * @see io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
   */
  fun query(query: ProcessInstancesByStateQuery): CompletableFuture<ProcessInstanceQueryResult> =
    queryGateway.query(
      query,
      QueryResponseMessageResponseType.queryResponseMessageResponseType<ProcessInstanceQueryResult>()
    )

}
