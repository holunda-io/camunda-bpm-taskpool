package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.polyflow.view.jpa.data.DataEntryEntity
import io.holunda.polyflow.view.jpa.data.toDataEntry
import io.holunda.polyflow.view.jpa.process.ProcessDefinitionEntity
import io.holunda.polyflow.view.jpa.process.ProcessInstanceEntity
import io.holunda.polyflow.view.jpa.process.toProcessDefinition
import io.holunda.polyflow.view.jpa.process.toProcessInstance
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
import org.axonframework.queryhandling.QueryUpdateEmitter

/**
 * Updates queries for process definitions.
 * @param entity entity to notify about.
 */
fun QueryUpdateEmitter.updateProcessDefinitionQuery(entity: ProcessDefinitionEntity) {
  val entry = entity.toProcessDefinition()

  JpaPolyflowViewService.logger.debug { "JPA-VIEW-44: Updating query with new element ${entry.processDefinitionId}" }

  this.emit(
    ProcessDefinitionsStartableByUserQuery::class.java,
    { query -> query.applyFilter(entry) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = listOf(entry)
    )
  )
}

/**
 * Updates subscription queries for data entries.
 * @param entity entity to notify about.
 * @param objectMapper objectMapper for conversion of payload.
 */
fun QueryUpdateEmitter.updateDataEntryQuery(entity: DataEntryEntity, objectMapper: ObjectMapper) {

  val entry = entity.toDataEntry(objectMapper)
  val revisionValue = RevisionValue(revision = entity.revision)

  JpaPolyflowViewService.logger.debug { "JPA-VIEW-43: Updating query with new element ${entry.identity} with revision $revisionValue" }

  this.emit(
    DataEntriesForUserQuery::class.java,
    { query -> query.applyFilter(entry) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = DataEntriesQueryResult(elements = listOf(entry)),
      metaData = revisionValue.toMetaData()
    )
  )
  this.emit(
    DataEntryForIdentityQuery::class.java,
    { query -> query.applyFilter(entry) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = DataEntriesQueryResult(elements = listOf(entry)),
      metaData = revisionValue.toMetaData()
    )
  )
  this.emit(
    DataEntriesQuery::class.java,
    { query -> query.applyFilter(entry) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = DataEntriesQueryResult(elements = listOf(entry)),
      metaData = revisionValue.toMetaData()
    )
  )
}

/**
 * Updates subscription queries for process instances.
 * @param entity entity to notify about.
 */
fun QueryUpdateEmitter.updateProcessInstanceQuery(entity: ProcessInstanceEntity) {
  val entry = entity.toProcessInstance()
  JpaPolyflowViewService.logger.debug { "JPA-VIEW-44: Updating query with new element ${entry.processInstanceId}" }
  this.emit(
    ProcessInstancesByStateQuery::class.java,
    { query -> query.applyFilter(entry) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = listOf(entry)
    )
  )
}
