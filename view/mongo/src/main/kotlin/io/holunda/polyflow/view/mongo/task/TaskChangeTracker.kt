package io.holunda.polyflow.view.mongo.task

import com.mongodb.MongoCommandException
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.mongo.ClearDeletedTasksMode
import io.holunda.polyflow.view.mongo.TaskPoolMongoViewProperties
import io.holunda.polyflow.view.mongo.data.DataEntryRepository
import io.holunda.polyflow.view.mongo.data.dataEntry
import io.holunda.polyflow.view.query.task.ApplicationWithTaskCount
import mu.KLogging
import org.bson.BsonValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.TriggerContext
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.util.retry.Retry
import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.logging.Level
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.random.Random
import kotlin.random.nextLong

/**
 * Observes the change stream on the mongo db and provides `Flux`es of changes for the various result types of queries. Also makes sure that tasks marked as
 * deleted are 'really' deleted shortly after.
 * Only active if `polyflow.view.mongo.changeTrackingMode` is set to `CHANGE_STREAM`.
 */
@Component
@ConditionalOnProperty(prefix = "polyflow.view.mongo", name = ["changeTrackingMode"], havingValue = "CHANGE_STREAM", matchIfMissing = false)
class TaskChangeTracker(
  private val taskRepository: TaskRepository,
  private val dataEntryRepository: DataEntryRepository,
  private val properties: TaskPoolMongoViewProperties,
  private val scheduler: TaskScheduler
) {
  companion object : KLogging()

  private var lastSeenResumeToken: BsonValue? = null

  private val changeStream: Flux<TaskDocument> = Flux.defer { taskRepository.getTaskUpdates(lastSeenResumeToken) }
    // When there are no more subscribers to the change stream, the flux is cancelled. When a new subscriber appears, they should not get any past updates.
    // This shouldn't happen at all because the `trulyDeleteChangeStream` subscription should always stay active, but we keep it as a last resort.
    .doOnCancel { lastSeenResumeToken = null }
    // Remember the last seen resume token if one is present
    .doOnNext { event -> lastSeenResumeToken = event.resumeToken ?: lastSeenResumeToken }
    // When the resume token is out of date, Mongo will throw an error 'resume of change stream was not possible, as the resume token was not found.'
    // Unfortunately, there is no way to identify exactly this error because error codes and messages vary by Mongo server version.
    // The closest we can get is reacting on any MongoCommandException and resetting the token so that upon the next retry, we start without a token.
    .doOnError(MongoCommandException::class.java) { lastSeenResumeToken = null }
    .doOnNext { event -> logger.debug { "Got event: $event" } }
    .log(TaskChangeTracker::class.qualifiedName, Level.WARNING, SignalType.ON_ERROR)
    .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100)).maxBackoff(Duration.ofSeconds(10)))
    .concatMap { event -> Mono.justOrEmpty(event.body) }
    .share()

  // Truly delete documents that have been marked deleted
  private val trulyDeleteChangeStreamSubscription: Disposable? =
    if (properties.changeStream.clearDeletedTasks.mode.usesChangeStream)
      changeStream
        .filter { it.deleted }
        .delayUntil {
          Mono.delay(
            Duration.between(
              scheduler.clock.instant(),
              (it.deleteTime ?: scheduler.clock.instant()).plus(properties.changeStream.clearDeletedTasks.after)
            )
          )
        }
        .flatMap({ deleteTask(it) }, 10)
        .subscribe()
    else
      null

  @PostConstruct
  fun initCleanupJob() {
    if (properties.changeStream.clearDeletedTasks.mode.usesCleanupJob) {
      scheduler.schedule(
        {
          taskRepository.findDeletedBefore(scheduler.clock.instant().minus(properties.changeStream.clearDeletedTasks.after))
            .flatMap({ deleteTask(it) }, 10)
            .subscribe()
        },
        CronTriggerWithJitter(
          properties.changeStream.clearDeletedTasks.jobSchedule,
          properties.changeStream.clearDeletedTasks.jobJitter,
          properties.changeStream.clearDeletedTasks.jobTimezone
        )
      )
    }
  }

  /**
   * Clear subscription.
   */
  @PreDestroy
  fun clearSubscription() {
    trulyDeleteChangeStreamSubscription?.dispose()
  }

  private fun deleteTask(task: TaskDocument): Mono<Void> {
    return taskRepository.deleteById(task.id)
      .doOnSuccess { logger.trace { "Deleted task ${task.id} from database." } }
      .doOnError { e -> logger.debug(e) { "Deleting task ${task.id} from database failed." } }
      .retryWhen(Retry.backoff(5, Duration.ofMillis(50)))
      .doOnError { e -> logger.warn(e) { "Deleting task ${task.id} from database failed and retries are exhausted." } }
      .onErrorResume { Mono.empty() }
  }

  /**
   * Adopt changes to task count by application stream.
   */
  fun trackTaskCountsByApplication(): Flux<ApplicationWithTaskCount> = changeStream
    .window(Duration.ofSeconds(1))
    .concatMap {
      it.reduce(setOf<String>()) { applicationNames, task ->
        applicationNames + task.sourceReference.applicationName
      }
    }
    .concatMap { Flux.fromIterable(it) }
    .concatMap { taskRepository.findTaskCountForApplication(it) }

  /**
   * Adopt changes to task update stream.
   */
  fun trackTaskUpdates(): Flux<Task> = changeStream
    .map { it.task() }

  /**
   * Adopt changes to task with data entries update stream.
   */
  fun trackTaskWithDataEntriesUpdates(): Flux<TaskWithDataEntries> = changeStream
    .concatMap { taskDocument ->
      val task = taskDocument.task()
      this.dataEntryRepository.findAllById(task.correlations.map { dataIdentityString(entryType = it.key, entryId = it.value.toString()) })
        .map { it.dataEntry() }
        .collectList()
        .map { TaskWithDataEntries(task = task, dataEntries = it) }
    }
}

data class CronTriggerWithJitter(val expression: CronExpression, val jitter: Duration, val zoneId: ZoneId = ZoneOffset.UTC) : Trigger {
  override fun nextExecutionTime(triggerContext: TriggerContext): Date? {
    val lastCompletionTime = triggerContext.lastCompletionTime()
    val lastCompletionTimeAdjusted = (lastCompletionTime?.coerceAtLeast(triggerContext.lastScheduledExecutionTime() ?: lastCompletionTime)?.toInstant()
      ?: triggerContext.clock.instant()).atZone(zoneId)
    val cronNextExecutionTime = expression.next(lastCompletionTimeAdjusted) ?: return null

    val offset = Random.nextLong(0..jitter.toNanos())
    return Date.from(cronNextExecutionTime.toInstant().plusNanos(offset))
  }
}

val ClearDeletedTasksMode.usesCleanupJob get() = this == ClearDeletedTasksMode.SCHEDULED_JOB || this == ClearDeletedTasksMode.BOTH
val ClearDeletedTasksMode.usesChangeStream get() = this == ClearDeletedTasksMode.CHANGE_STREAM_SUBSCRIPTION || this == ClearDeletedTasksMode.BOTH
