package io.holunda.polyflow.view.mongo.data

import com.mongodb.MongoCommandException
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.mongo.TaskPoolMongoViewProperties
import io.holunda.polyflow.view.mongo.util.CronTriggerWithJitter
import mu.KLogging
import org.bson.BsonValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.BufferOverflowStrategy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.core.scheduler.Schedulers
import reactor.util.retry.Retry
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Tracks changes of data entries. Also makes sure that data entries marked as deleted are 'really' deleted shortly after.
 * Only active if `polyflow.view.mongo.changeTrackingMode` is set to `CHANGE_STREAM`.
 */
@Component
@ConditionalOnProperty(prefix = "polyflow.view.mongo", name = ["changeTrackingMode"], havingValue = "CHANGE_STREAM", matchIfMissing = false)
class DataEntryChangeTracker(
  private val dataEntryRepository: DataEntryRepository,
  private val properties: TaskPoolMongoViewProperties,
  private val scheduler: TaskScheduler
) {

  companion object : KLogging()

  private var lastSeenResumeToken: BsonValue? = null

  private val changeStream: Flux<DataEntryDocument> = Flux
    .defer { dataEntryRepository.getDataEntryUpdates(lastSeenResumeToken) }
    // When there are no more subscribers to the change stream, the flux is cancelled. When a new subscriber appears, they should not get any past updates.
    .doOnCancel { lastSeenResumeToken = null }
    // Remember the last seen resume token if one is present
    .doOnNext { event -> lastSeenResumeToken = event.resumeToken ?: lastSeenResumeToken }
    // When the resume token is out of date, Mongo will throw an error 'resume of change stream was not possible, as the resume token was not found.'
    // Unfortunately, there is no way to identify exactly this error because error codes and messages vary by Mongo server version.
    // The closest we can get is reacting on any MongoCommandException and resetting the token so that upon the next retry, we start without a token.
    .doOnError(MongoCommandException::class.java) { lastSeenResumeToken = null }
    .doOnNext { event -> logger.debug { "Got event: $event" } }
    .log(DataEntryChangeTracker::class.qualifiedName, Level.WARNING, SignalType.ON_ERROR)
    .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100)).maxBackoff(Duration.ofSeconds(10)))
    .concatMap { event -> Mono.justOrEmpty(event.body) }
    .share()

  // Truly delete documents that have been marked deleted
  private val trulyDeleteChangeStreamSubscription: Disposable? =
    if (properties.changeStream.clearDeletedDataEntries.mode.usesChangeStream)
      changeStream
        .filter { it.deleted }
        .map { it.deleteTime to it.identity }
        .onBackpressureBuffer(
          properties.changeStream.clearDeletedDataEntries.bufferSize,
          { (_, dataEntryId) -> logger.warn { "Too many data entries waiting to be deleted from MongoDB after being marked as deleted. Dropping latest data entry $dataEntryId - manual cleanup may be required." } },
          BufferOverflowStrategy.DROP_LATEST
        )
        .delayUntil { (deleteTime, _) ->
          Mono.delay(
            Duration.between(
              now(),
              (deleteTime ?: now()).plus(properties.changeStream.clearDeletedDataEntries.after)
            )
          )
        }
        .flatMap({ (_, dataEntryId) -> deleteDataEntry(dataEntryId) }, 10)
        .subscribe()
    else
      null

  // This will return the current time of the VirtualTimeScheduler if that is enabled in a test.
  private fun now() = Instant.ofEpochMilli(Schedulers.parallel().now(TimeUnit.MILLISECONDS))

  /**
   * Initializes scheduling of the clean-up if configured via properties.
   */
  @PostConstruct
  fun initCleanupJob() {
    if (properties.changeStream.clearDeletedDataEntries.mode.usesCleanupJob) {
      scheduler.schedule(
        {
          dataEntryRepository.findDeletedBefore(scheduler.clock.instant().minus(properties.changeStream.clearDeletedDataEntries.after))
            .flatMap({ deleteDataEntry(it.identity) }, 10)
            .subscribe()
        },
        CronTriggerWithJitter(
          CronExpression.parse(properties.changeStream.clearDeletedDataEntries.jobSchedule),
          properties.changeStream.clearDeletedDataEntries.jobJitter,
          properties.changeStream.clearDeletedDataEntries.jobTimezone
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

  private fun deleteDataEntry(dataEntryId: String): Mono<Void> {
    return dataEntryRepository.deleteById(dataEntryId)
      .doOnSuccess { logger.trace { "Deleted data entry $dataEntryId from database." } }
      .doOnError { e -> logger.debug(e) { "Deleting data entry $dataEntryId from database failed." } }
      .retryWhen(Retry.backoff(5, Duration.ofMillis(50)))
      .doOnError { e -> logger.warn(e) { "Deleting data entry $dataEntryId from database failed and retries are exhausted." } }
      .onErrorResume { Mono.empty() }
  }

  /**
   * Retrieves a flux of data entries updates.
   *
   * @return data entry updates stream.
   */
  fun trackDataEntryUpdates(): Flux<DataEntry> = changeStream.map { it.dataEntry() }

}
