package io.holunda.polyflow.view.mongo.service

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.polyflow.view.mongo.process.ProcessDefinitionDocument
import io.holunda.polyflow.view.mongo.process.ProcessDefinitionRepository
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import io.holunda.polyflow.view.query.process.ReactiveProcessDefinitionApi
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * Mongo process definition projection.
 */
@Component
class ProcessDefinitionMongoService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val processDefinitionRepository: ProcessDefinitionRepository
) : ReactiveProcessDefinitionApi {

  companion object : KLogging()

  /**
   * On new process definition.
   */
  @EventHandler
  @Suppress("unused")
  fun on(event: ProcessDefinitionRegisteredEvent, metaData: MetaData) {

    logger.debug { "New process definition with id ${event.processDefinitionId} registered (${event.processName}, ${event.applicationName})." }

    val entry = ProcessDefinitionDocument(
      processDefinitionId = event.processDefinitionId,
      processDefinitionKey = event.processDefinitionKey,
      processDefinitionVersion = event.processDefinitionVersion,
      processDescription = event.processDescription,
      processName = event.processName,
      processVersionTag = event.processVersionTag,
      applicationName = event.applicationName,
      candidateStarterGroups = event.candidateStarterGroups,
      candidateStarterUsers = event.candidateStarterUsers,
      formKey = event.formKey,
      startableFromTasklist = event.startableFromTasklist
    )

    processDefinitionRepository.save(entry)
      .doOnNext {
        queryUpdateEmitter.emit(
          ProcessDefinitionsStartableByUserQuery::class.java,
          { query -> query.applyFilter(entry.toProcessDefinition()) },
          entry
        )
      }
      .block()
  }

  @QueryHandler
  override fun query(query: ProcessDefinitionsStartableByUserQuery, metaData: MetaData): CompletableFuture<List<ProcessDefinition>> {
    // This is the naive Kotlin way, not the Mongo way it should be. Please don't laugh.
    // Get all definitions in all versions and group them by process
    return processDefinitionRepository
      .findAll()
      .collectList()
      .map { it.groupBy { processDefinition -> processDefinition.processDefinitionKey } }
      .map { processesByDefinition ->
        // Find the most current version of each process
        val currentProcessDefinitions = processesByDefinition.map {
          it.value.maxByOrNull { definition -> definition.processDefinitionVersion }!!
        }

        // Apply filter
        currentProcessDefinitions
          .map { it.toProcessDefinition() }
          .filter { query.applyFilter(it) }
      }
      .toFuture()

  }
}
