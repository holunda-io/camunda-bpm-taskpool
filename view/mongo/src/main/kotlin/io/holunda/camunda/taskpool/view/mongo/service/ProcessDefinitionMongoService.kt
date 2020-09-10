package io.holunda.camunda.taskpool.view.mongo.service

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.mongo.repository.ProcessDefinitionDocument
import io.holunda.camunda.taskpool.view.mongo.repository.ProcessDefinitionRepository
import io.holunda.camunda.taskpool.view.query.ReactiveProcessDefinitionApi
import io.holunda.camunda.taskpool.view.query.process.ProcessDefinitionsStartableByUserQuery
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * Mongo proces sdefinition projection.
 */
@Component
class ProcessDefinitionMongoService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val processDefinitionRepository: ProcessDefinitionRepository
) : ReactiveProcessDefinitionApi {

  companion object : KLogging()

  @EventHandler
  @Suppress("unused")
  fun on(event: ProcessDefinitionRegisteredEvent) {

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
      .doOnNext { queryUpdateEmitter.emit(ProcessDefinitionsStartableByUserQuery::class.java, { query -> query.applyFilter(entry.toProcessDefitinion()) }, entry) }
      .block()
  }

  @QueryHandler
  override fun query(query: ProcessDefinitionsStartableByUserQuery): CompletableFuture<List<ProcessDefinition>> {
    // This is the naive Kotlin way, not the Mongo way it should be. Please don't laugh.
    // Get all definitions in all versions and group them by process
    return processDefinitionRepository
      .findAll()
      .collectList()
      .map { it.groupBy { processDefinition -> processDefinition.processDefinitionKey } }
      .map { processesByDefinition ->
        // Find the most current version of each process
        val currentProcessDefinitions = processesByDefinition.map {
          it.value.maxBy { definition -> definition.processDefinitionVersion }!!
        }

        // Apply filter
        currentProcessDefinitions
          .map { it.toProcessDefitinion() }
          .filter { query.applyFilter(it) }
      }
      .toFuture()

  }
}
