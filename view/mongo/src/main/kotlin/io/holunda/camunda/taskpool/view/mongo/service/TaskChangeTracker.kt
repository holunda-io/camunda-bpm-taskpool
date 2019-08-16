package io.holunda.camunda.taskpool.view.mongo.service

import com.mongodb.client.model.changestream.OperationType
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.mongo.repository.*
import io.holunda.camunda.taskpool.view.query.task.ApplicationWithTaskCount
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Observes the change stream on the mongo db and provides `Flux`es of changes for the various result types of queries. Also makes sure that tasks marked as
 * deleted are 'really' deleted shortly after.
 * Only active if `camunda.taskpool.view.mongo.changeTrackingMode` is set to `CHANGE_STREAM`.
 */
@Component
@ConditionalOnProperty(prefix = "camunda.taskpool.view.mongo", name = ["changeTrackingMode"], havingValue = "CHANGE_STREAM", matchIfMissing = false)
class TaskChangeTracker(
  private val taskRepository: TaskRepository,
  private val dataEntryRepository: DataEntryRepository
) {
  companion object : KLogging()

  private lateinit var changeStream: Flux<ChangeStreamEvent<TaskDocument>>
  private lateinit var trulyDeleteChangeStreamSubscription: Disposable

  @PostConstruct
  fun subscribeTaskCountForApplication() {
    changeStream = taskRepository.getTaskUpdates()
      .filter { event ->
        when (event.operationType) {
          OperationType.INSERT, OperationType.UPDATE, OperationType.REPLACE -> {
            logger.debug { "Got ${event.operationType?.value} event: $event" }
            true
          }
          else -> {
            logger.trace { "Ignoring ${event.operationType?.value} event: $event" }
            false
          }
        }
      }
      .share()

    // Truly delete documents that have been marked deleted
    trulyDeleteChangeStreamSubscription = changeStream
      .filter { event -> event.body?.deleted == true }
      .flatMap { event ->
        taskRepository.deleteById(event.body.id)
          .doOnSuccess { logger.trace { "Deleted task ${event.body.id} from database." } }
      }
      .subscribe()
  }

  @PreDestroy
  fun clearSubscription() {
    trulyDeleteChangeStreamSubscription.dispose()
  }

  fun trackTaskCountsByApplication(): Flux<ApplicationWithTaskCount> = changeStream
    .flatMap { event ->
      val applicationName = event.body.sourceReference.applicationName
      taskRepository.findTaskCountForApplication(applicationName)
    }

  fun trackTaskUpdates(): Flux<Task> = changeStream
    .map { event -> event.body.task() }

  fun trackTaskWithDataEntriesUpdates(): Flux<TaskWithDataEntries> = changeStream
    .flatMap { event ->
      val task = event.body.task()
      this.dataEntryRepository.findAllById(task.correlations.map { dataIdentityString(entryType = it.key, entryId = it.value.toString()) })
        .map { it.dataEntry() }
        .collectList()
        .map { TaskWithDataEntries(task = task, dataEntries = it) }
    }
}
