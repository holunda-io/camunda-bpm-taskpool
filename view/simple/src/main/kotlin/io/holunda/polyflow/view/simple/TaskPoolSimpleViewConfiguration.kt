package io.holunda.polyflow.view.simple

import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.QueryResult
import io.holunda.polyflow.view.simple.service.SimpleServiceViewProcessingGroup
import jakarta.annotation.PostConstruct
import mu.KLogging
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Configuration for in-memory polyflow view.
 */
@ComponentScan
@Configuration
class TaskPoolSimpleViewConfiguration {

  companion object : KLogging()

  /**
   * Configures the in-memory (simple) view to use an in-memory token store, to make sure that the
   * token and the projection are stored in the same place.
   *
   * This is required to get independent from the globally configured token store (which is JPA, Mongo, or whatever).
   */
  @Autowired
  fun configure(eventProcessingConfigurer: EventProcessingConfigurer) {
    val processorName = "in-mem-processor"
    eventProcessingConfigurer.registerTokenStore(processorName) { InMemoryTokenStore() }
    eventProcessingConfigurer.assignProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP, processorName)
  }

  /**
   * Logs a little.
   */
  @PostConstruct
  fun info() {
    logger.info { "VIEW-SIMPLE-001: Initialized simple view" }
  }
}


/**
 * Update query if the element is reset in the map.
 * @param map containing elements
 * @param key a key of the updated element in the map
 * @param queryClazz class of the query to apply to.
 * @param [T] type of entry.
 * @param [Q] type of filter query, capable to filter relevant elements.
 */
fun <T : Any, Q : FilterQuery<T>> QueryUpdateEmitter.updateMapFilterQuery(map: Map<String, T>, key: String, queryClazz: Class<Q>) {
  if (map.contains(key)) {
    val entry = map.getValue(key)
    this.emit(queryClazz, { query -> query.applyFilter(entry) }, entry)
  }
}

/**
 * Update query if the element is reset in the map.
 * @param map containing elements
 * @param key a key of the updated element in the map
 * @param queryClazz class of the query to apply to.
 * @param queryResultFactory factory to produce the query result of of entry from the map.
 * @param [T] type of entry.
 * @param [Q] type of filter query, capable to filter relevant elements.
 * @param [QR] type of query result.
 */
fun <T : Any, Q : FilterQuery<T>, QR : QueryResult<T, QR>> QueryUpdateEmitter.updateMapFilterQuery(
  map: Map<String, T>,
  key: String,
  queryClazz: Class<Q>,
  queryResultFactory: (T) -> QR
) {
  if (map.contains(key)) {
    val entry = map.getValue(key)
    this.emit(queryClazz, { query -> query.applyFilter(entry) }, queryResultFactory.invoke(entry))
  }
}
