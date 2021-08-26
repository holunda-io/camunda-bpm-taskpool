package io.holunda.polyflow.view.jpa.update

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.ProcessInstance
import io.holunda.polyflow.view.jpa.*
import io.holunda.polyflow.view.jpa.EventEmittingType.BEFORE_COMMIT
import io.holunda.polyflow.view.jpa.EventEmittingType.DIRECT
import io.holunda.polyflow.view.jpa.data.DataEntryEntity
import io.holunda.polyflow.view.jpa.data.toDataEntry
import io.holunda.polyflow.view.jpa.process.ProcessDefinitionEntity
import io.holunda.polyflow.view.jpa.process.ProcessInstanceEntity
import io.holunda.polyflow.view.jpa.process.toProcessDefinition
import io.holunda.polyflow.view.jpa.process.toProcessInstance
import mu.KLogging
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Component responsible for sending the query updates based on configuration.
 */
@Component
class TxAwareQueryUpdateEmitter(
  val queryUpdateEmitter: QueryUpdateEmitter,
  val objectMapper: ObjectMapper,
  val polyflowJpaViewProperties: PolyflowJpaViewProperties
) {

  companion object : KLogging()

  // Thread-local registration of the transaction synchronization.
  private val registered: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

  // Thread local updates
  private val dataEntries: ThreadLocal<MutableList<Pair<DataEntry, RevisionValue>>> = ThreadLocal.withInitial { mutableListOf() }
  private val processInstances: ThreadLocal<MutableList<ProcessInstance>> = ThreadLocal.withInitial { mutableListOf() }
  private val processDefinitions: ThreadLocal<MutableList<ProcessDefinition>> = ThreadLocal.withInitial { mutableListOf() }


  /**
   * Sends the update for the query.
   */
  internal fun emitEntityUpdate(entity: Any) {

    // add entry
    when (entity) {
      is DataEntryEntity -> entity.toDataEntry(objectMapper)
        .also { dataEntry -> dataEntries.get().add(dataEntry to RevisionValue(entity.revision)) }
      is ProcessInstanceEntity -> entity.toProcessInstance()
        .also { processInstance -> processInstances.get().add(processInstance) }
      is ProcessDefinitionEntity -> entity.toProcessDefinition()
        .also { processDefinition -> processDefinitions.get().add(processDefinition) }
    }

    if (polyflowJpaViewProperties.eventEmittingType == DIRECT) {
      send()
    } else {
      // register synchronization only once
      if (!registered.get()) {
        // send the result inside the transaction synchronization
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
          /**
           * Execute send if flag is set to send inside the TX.
           */
          override fun beforeCommit(readOnly: Boolean) {
            if (polyflowJpaViewProperties.eventEmittingType == BEFORE_COMMIT) {
              send()
            }
          }
          /**
           * Execute send if flag is set to send outside the TX.
           */
          override fun afterCommit() {
            if (polyflowJpaViewProperties.eventEmittingType == EventEmittingType.AFTER_COMMIT) {
              send()
            }
          }
          /**
           * Clean-up the thread on completion.
           */
          override fun afterCompletion(status: Int) {
            dataEntries.remove()
            processDefinitions.remove()
            processInstances.remove()
            registered.remove()
          }
        })
        // mark as registered
        registered.set(true)
      }
    }
  }

  private fun send() {
    dataEntries.get().forEach { (dataEntry, revision) ->
      logger.debug { "JPA-VIEW-44: Updating query with new element $dataEntry" }
      queryUpdateEmitter.updateDataEntryQuery(dataEntry, revision)
    }
    processInstances.get().forEach { processInstance ->
      logger.debug { "JPA-VIEW-45: Updating query with new element $processInstance" }
      queryUpdateEmitter.updateProcessInstanceQuery(processInstance)
    }
    processDefinitions.get().forEach { processDefinition ->
      logger.debug { "JPA-VIEW-46: Updating query with new element $processDefinition" }
      queryUpdateEmitter.updateProcessDefinitionQuery(processDefinition)
    }
  }
}

