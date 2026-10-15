package io.holunda.polyflow.view.jpa

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionSuspendedEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionActivatedEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionUnregisteredEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.view.ProcessDefinitionState
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
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Implementation of the Polyflow Process Definition View API using JPA to create the persistence model.
 */
@Component
@ProcessingGroup(PROCESSING_GROUP)
class JpaPolyflowViewProcessDefinitionService(
  val processDefinitionRepository: ProcessDefinitionRepository,
  val queryUpdateEmitter: QueryUpdateEmitter,
  val polyflowJpaViewProperties: PolyflowJpaViewProperties,
  val objectMapper: ObjectMapper,
) : ProcessDefinitionApi {

  /**
   * Static constants.
   */
  companion object {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.jpa.service.process.definition"
  }

  @QueryHandler
  override fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition> {
    val authorizedPrincipals: Set<AuthorizationPrincipal> = setOf(user(query.user.username)).plus(query.user.groups.map { group(it) })
    return processDefinitionRepository.findAll(isStarterAuthorizedFor(authorizedPrincipals)).map { it.toProcessDefinition(objectMapper) }
  }

  /**
   * Registers a new process definition.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessDefinitionRegisteredEvent) {
    if (isDisabledByProperty()) return

    val entity = processDefinitionRepository.save(
      event.toEntity(objectMapper)
    )
    emitProcessDefinitionUpdate(entity)
  }

  /**
   * Stores the suspended lifecycle state.
   */
  @EventHandler
  fun on(event: ProcessDefinitionSuspendedEvent) = setState(event.processDefinitionId, ProcessDefinitionState.SUSPENDED)

  /**
   * Stores the active lifecycle state.
   */
  @EventHandler
  fun on(event: ProcessDefinitionActivatedEvent) = setState(event.processDefinitionId, ProcessDefinitionState.ACTIVE)

  /**
   * Deletes the unregistered definition projection.
   */
  @EventHandler
  fun on(event: ProcessDefinitionUnregisteredEvent) { processDefinitionRepository.deleteById(event.processDefinitionId) }

  private fun setState(id: String, state: ProcessDefinitionState) {
    processDefinitionRepository.findById(id).ifPresent { entity ->
      entity.state = state
      emitProcessDefinitionUpdate(processDefinitionRepository.save(entity))
    }
  }

  private fun emitProcessDefinitionUpdate(entity: ProcessDefinitionEntity) {
    queryUpdateEmitter.updateProcessDefinitionQuery(entity.toProcessDefinition(objectMapper))
  }

  private fun isDisabledByProperty(): Boolean {
    return (!polyflowJpaViewProperties.storedItems.contains(StoredItem.PROCESS_DEFINITION)).also {
      if (it) {
        logger.debug { "Process definition storage disabled by property." }
      }
    }
  }

}
