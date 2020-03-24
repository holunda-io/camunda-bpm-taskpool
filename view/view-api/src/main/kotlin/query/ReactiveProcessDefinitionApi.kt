package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.query.process.ProcessDefinitionsStartableByUserQuery
import java.util.concurrent.CompletableFuture

/**
 * Reactive API for process definitions.
 */
interface ReactiveProcessDefinitionApi {

  /**
   * Query for startable process definitions.
   * @param query query object.
   * @return observable list of process definitions.
   */
  fun query(query: ProcessDefinitionsStartableByUserQuery): CompletableFuture<List<ProcessDefinition>>
}
