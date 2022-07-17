package io.holunda.polyflow.view

import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import io.holunda.polyflow.view.query.task.*
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture

/**
 * Client encapsulating the correct query types (including response types)
 */
class ProcessDefinitionQueryClient(
  private val queryGateway: QueryGateway
) {

  /**
   * @see io.holunda.polyflow.view.query.process.ProcessDefinitionApi.query
   * @see io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
   */
  fun query(query: ProcessDefinitionsStartableByUserQuery): CompletableFuture<List<ProcessDefinition>> =
    queryGateway.query(
      query,
      ResponseTypes.multipleInstancesOf(ProcessDefinition::class.java)
    )

}
