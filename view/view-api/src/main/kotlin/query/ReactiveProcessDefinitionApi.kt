package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.query.process.ProcessDefinitionsStartableByUserQuery
import org.axonframework.messaging.MetaData
import java.util.concurrent.CompletableFuture

/**
 * Reactive API for process definitions.
 */
interface ReactiveProcessDefinitionApi {

  /**
   * Query for startable process definitions.
   * @param query query object.
   * @param metaData query metaData, may be empty.
   * @return observable list of process definitions.
   */
  fun query(query: ProcessDefinitionsStartableByUserQuery, metaData: MetaData = MetaData.emptyInstance()): CompletableFuture<List<ProcessDefinition>>
}
