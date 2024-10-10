package io.holunda.polyflow.view.jpa

import io.holunda.camunda.taskpool.api.business.DataEntryAnonymizedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryDeletedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.jpa.data.DataEntryEventHandler
import io.holunda.polyflow.view.jpa.data.DataEntryRepository
import io.holunda.polyflow.view.jpa.process.ProcessDefinitionRepository
import io.holunda.polyflow.view.jpa.process.ProcessInstanceRepository
import io.holunda.polyflow.view.jpa.process.SourceReferenceEmbeddable
import io.holunda.polyflow.view.jpa.task.TaskEntity
import io.holunda.polyflow.view.jpa.task.TaskRepository
import io.holunda.polyflow.view.query.data.*
import org.axonframework.eventhandling.Timestamp
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryResponseMessage
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

data class Pojo(
  val attribute1: String,
  val attribute2: Date
)


fun emptyTask() = TaskEntity(
  taskId = "id-4711",
  taskDefinitionKey = "task.def.0815",
  name = "task name",
  priority = 50,
  sourceReference = processReference(),
  payload = null
)

fun processReference() = SourceReferenceEmbeddable(
  instanceId = "instance-1283947",
  executionId = "execution-4568789",
  definitionId = "12313-12343-34244-23423:13",
  definitionKey = "12313-12343-34244-23423",
  name = "process",
  applicationName = "test-application",
  sourceType = "PROCESS"
)

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
class DbCleaner(
  val dataEntryRepository: DataEntryRepository,
  val processDefinitionRepository: ProcessDefinitionRepository,
  val processInstanceRepository: ProcessInstanceRepository,
  val taskRepository: TaskRepository
) {

  fun cleanup() {
    dataEntryRepository.deleteAll()
    taskRepository.deleteAll()
    processDefinitionRepository.deleteAll()
    processInstanceRepository.deleteAll()
  }
}

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
class JpaPolyflowViewServiceTxFacade(private val implementation: JpaPolyflowViewDataEntryService) : DataEntryApi, DataEntryEventHandler {

  override fun query(query: DataEntriesForDataEntryTypeQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> =
    implementation.query(query = query, metaData = metaData)

  override fun query(query: DataEntryForIdentityQuery, metaData: MetaData): QueryResponseMessage<DataEntry> =
    implementation.query(query = query, metaData = metaData)

  override fun query(query: DataEntriesForUserQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> =
    implementation.query(query = query, metaData = metaData)

  override fun query(query: DataEntriesQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> =
    implementation.query(query = query, metaData = metaData)

  override fun on(event: DataEntryCreatedEvent, metaData: MetaData, @Timestamp eventTimestamp: Instant) = implementation.on(event = event, metaData = metaData, eventTimestamp)

  override fun on(event: DataEntryUpdatedEvent, metaData: MetaData, @Timestamp eventTimestamp: Instant) = implementation.on(event = event, metaData = metaData, eventTimestamp)

  override fun on(event: DataEntryDeletedEvent, metaData: MetaData) = implementation.on(event = event, metaData = metaData)

  override fun on(event: DataEntryAnonymizedEvent, metaData: MetaData) = implementation.on(event = event, metaData = metaData)
}
