package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.query.ProcessDefinitionApi
import io.holunda.camunda.taskpool.view.query.process.ProcessDefinitionsStartableByUserQuery
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple projection for process definitions.
 */
@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class ProcessDefinitionSimpleService(
  private val queryUpdateEmitter: QueryUpdateEmitter
) : ProcessDefinitionApi {

  companion object : KLogging()

  private val processDefinitions: MutableMap<String, TreeSet<ProcessDefinition>> = ConcurrentHashMap()


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
      .getOrPut(event.processDefinitionKey) { TreeSet(kotlin.Comparator { val1, val2 -> val1.processDefinitionVersion.compareTo(val2.processDefinitionVersion) }) }
      .add(entry)

    queryUpdateEmitter.emit(ProcessDefinitionsStartableByUserQuery::class.java, { query -> query.applyFilter(entry) }, entry)
  }

  @QueryHandler
  override fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition> =
    processDefinitions
      .values
      .map { it.last() }
      .filter { query.applyFilter(it) }
}
