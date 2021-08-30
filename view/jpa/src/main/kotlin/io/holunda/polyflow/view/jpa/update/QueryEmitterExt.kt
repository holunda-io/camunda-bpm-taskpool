package io.holunda.polyflow.view.jpa

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.polyflow.view.*
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import io.holunda.polyflow.view.query.process.ProcessInstanceQueryResult
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
import io.holunda.polyflow.view.query.task.*
import org.axonframework.queryhandling.QueryUpdateEmitter


/**
 * Updates queries for process definitions.
 * @param entry entry to notify about.
 */
fun QueryUpdateEmitter.updateProcessDefinitionQuery(entry: ProcessDefinition) {
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
 * @param entry entry to notify about.
 * @param revisionValue revision.
 */
fun QueryUpdateEmitter.updateDataEntryQuery(entry: DataEntry, revisionValue: RevisionValue) {

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
 * @param entry entry to notify about.
 */
fun QueryUpdateEmitter.updateProcessInstanceQuery(entry: ProcessInstance) {
  this.emit(
    ProcessInstancesByStateQuery::class.java,
    { query -> query.applyFilter(entry) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = ProcessInstanceQueryResult(listOf(entry))
    )
  )
}

/**
 * Updates subscription queries for tasks.
 * @param task task to update.
 */
fun QueryUpdateEmitter.updateTaskQuery(entry: TaskWithDataEntries) {

  this.emit(
    TaskForIdQuery::class.java,
    { query -> query.applyFilter(entry.task) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = entry.task
    )
  )

  this.emit(
    TasksForApplicationQuery::class.java,
    { query -> query.applyFilter(entry.task) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = TaskQueryResult(elements = listOf(entry.task))
    )
  )

  this.emit(
    TasksForUserQuery::class.java,
    { query -> query.applyFilter(entry.task) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = TaskQueryResult(elements = listOf(entry.task))
    )
  )

  this.emit(
    TaskWithDataEntriesForIdQuery::class.java,
    { query -> query.applyFilter(entry) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = entry
    )
  )

  this.emit(
    TasksWithDataEntriesForUserQuery::class.java,
    { query -> query.applyFilter(entry) },
    QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
      payload = TasksWithDataEntriesQueryResult(
        elements = listOf(entry)
      )
    )
  )

}
