package io.holunda.polyflow.view.query.process

import io.holunda.polyflow.view.ProcessDefinition
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
