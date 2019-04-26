package io.holunda.camunda.taskpool.view.mongo.service

import io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.mongo.repository.ProcessDefinitionDocument
import io.holunda.camunda.taskpool.view.mongo.repository.ProcessDefinitionRepository
import io.holunda.camunda.taskpool.view.query.ProcessDefinitionApi
import io.holunda.camunda.taskpool.view.query.ProcessDefinitionsStartableByUserQuery
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
open class ProcessDefinitionMongoService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val processDefinitionRepository: ProcessDefinitionRepository
) : ProcessDefinitionApi {

  companion object : KLogging()

  @EventHandler
  @Suppress("unused")
  open fun on(event: ProcessDefinitionRegisteredEvent) {

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

    queryUpdateEmitter.emit(ProcessDefinitionsStartableByUserQuery::class.java, { query -> query.applyFilter(entry.toProcessDefitinion()) }, entry)
  }

  @QueryHandler
  override fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition> {
    // This is the naive Kotlin way, not the Mongo way it should be. Please don't laugh.
    // Get all definitions in all versions and group them by process
    val processesByDefinition: Map<String, List<ProcessDefinitionDocument>> = processDefinitionRepository
      .findAll()
      .groupBy { processDefinition -> processDefinition.processDefinitionKey }

    // Find the most current version of each process
    val currentProcessDefinitions = processesByDefinition.map {
      it.value.sortedBy { definition -> definition.processDefinitionVersion }.last()
    }

    // Apply filter
    return currentProcessDefinitions
      .map { it.toProcessDefitinion() }
      .filter { query.applyFilter(it) }
  }
}
