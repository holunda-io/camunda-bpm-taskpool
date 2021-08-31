package io.holunda.polyflow.view.jpa

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.jpa.JpaPolyflowViewProcessDefinitionService.Companion.PROCESSING_GROUP
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.process.ProcessDefinitionEntity
import io.holunda.polyflow.view.jpa.process.ProcessDefinitionRepository
import io.holunda.polyflow.view.jpa.process.ProcessDefinitionRepository.Companion.isStarterAuthorizedFor
import io.holunda.polyflow.view.jpa.process.toEntity
import io.holunda.polyflow.view.jpa.process.toProcessDefinition
import io.holunda.polyflow.view.jpa.update.updateProcessDefinitionQuery
import io.holunda.polyflow.view.query.process.ProcessDefinitionApi
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

/**
 * Implementation of the Polyflow Process Definition View API using JPA to create the persistence model.
 */
@Component
@ProcessingGroup(PROCESSING_GROUP)
class JpaPolyflowViewProcessDefinitionService(
  val processDefinitionRepository: ProcessDefinitionRepository,
  val queryUpdateEmitter: QueryUpdateEmitter,
  val polyflowJpaViewProperties: PolyflowJpaViewProperties
) : ProcessDefinitionApi {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.jpa.service.process.definition"
  }

  @QueryHandler
  override fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition> {
    val authorizedPrincipals: Set<AuthorizationPrincipal> = setOf(user(query.user.username)).plus(query.user.groups.map { group(it) })
    return processDefinitionRepository.findAll(isStarterAuthorizedFor(authorizedPrincipals)).map { it.toProcessDefinition() }
  }

  /**
   * Registers a new process definition.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessDefinitionRegisteredEvent) {
    if (!polyflowJpaViewProperties.storedItems.contains(StoredItem.PROCESS_DEFINITION)) {
      logger.debug { "Process definition storage disabled by property." }
      return
    }

    val entity = processDefinitionRepository.save(
      event.toEntity()
    )
    emitProcessDefinitionUpdate(entity)
  }

  private fun emitProcessDefinitionUpdate(entity: ProcessDefinitionEntity) {
    queryUpdateEmitter.updateProcessDefinitionQuery(entity.toProcessDefinition())
  }

}
