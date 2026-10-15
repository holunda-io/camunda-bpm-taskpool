package io.holunda.polyflow.view.mongo.process

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionSuspendedEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionActivatedEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionUnregisteredEvent
import io.holunda.polyflow.view.ProcessDefinitionState
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import io.holunda.polyflow.view.query.process.ReactiveProcessDefinitionApi
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

/**
 * Mongo process definition projection.
 */
@Component
class ProcessDefinitionMongoService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val processDefinitionRepository: ProcessDefinitionRepository
) : ReactiveProcessDefinitionApi {

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
      startableFromTasklist = event.startableFromTasklist,
      payload = event.payload.toMap(),
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

  /**
   * Marks the definition as suspended.
   */
  @EventHandler
  fun on(event: ProcessDefinitionSuspendedEvent) = updateState(event.processDefinitionId, ProcessDefinitionState.SUSPENDED)

  /**
   * Marks the definition as active.
   */
  @EventHandler
  fun on(event: ProcessDefinitionActivatedEvent) = updateState(event.processDefinitionId, ProcessDefinitionState.ACTIVE)

  /**
   * Deletes the unregistered definition document.
   */
  @EventHandler
  fun on(event: ProcessDefinitionUnregisteredEvent) { processDefinitionRepository.deleteById(event.processDefinitionId).block() }

  private fun updateState(id: String, state: ProcessDefinitionState) {
    processDefinitionRepository.findById(id).map { it.copy(state = state) }.flatMap(processDefinitionRepository::save).block()
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
