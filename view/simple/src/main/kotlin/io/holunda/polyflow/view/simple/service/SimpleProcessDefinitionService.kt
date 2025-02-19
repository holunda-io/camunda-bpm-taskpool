package io.holunda.polyflow.view.simple.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.query.process.ProcessDefinitionApi
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Simple projection for process definitions.
 */
@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class SimpleProcessDefinitionService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val processDefinitions: MutableMap<String, TreeSet<ProcessDefinition>> = ConcurrentHashMap()
) : ProcessDefinitionApi {

  /**
   * React on process definition registration.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessDefinitionRegisteredEvent) {

    logger.debug { "New process definition with id ${event.processDefinitionId} registered (${event.processName}, ${event.applicationName})." }

    val entry = ProcessDefinition(
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

    processDefinitions
      .getOrPut(event.processDefinitionKey) { TreeSet { val1, val2 -> val1.processDefinitionVersion.compareTo(val2.processDefinitionVersion) } }
      .add(entry)

    queryUpdateEmitter.emit(ProcessDefinitionsStartableByUserQuery::class.java, { query -> query.applyFilter(entry) }, entry)
  }

  @QueryHandler
  override fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition> =
    processDefinitions
      .values
      .map { it.last() }
      .filter { query.applyFilter(it) }

  /**
   * Read-only stored data.
   */
  fun getProcessDefinitions(): Map<String, Set<ProcessDefinition>> = processDefinitions.toMap()
}
