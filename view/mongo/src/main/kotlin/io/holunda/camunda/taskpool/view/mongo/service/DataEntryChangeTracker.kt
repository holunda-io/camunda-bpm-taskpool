package io.holunda.camunda.taskpool.view.mongo.service

import com.mongodb.MongoCommandException
import com.mongodb.client.model.changestream.OperationType
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.mongo.repository.DataEntryDocument
import io.holunda.camunda.taskpool.view.mongo.repository.DataEntryUpdateRepository
import io.holunda.camunda.taskpool.view.mongo.repository.dataEntry
import mu.KLogging
import org.bson.BsonValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.util.retry.Retry
import java.time.Duration
import java.util.*
import java.util.logging.Level

/**
 * Tracks changes of data entries.
 * Only active if `polyflow.view.mongo.changeTrackingMode` is set to `CHANGE_STREAM`.
 */
@Component
@ConditionalOnProperty(prefix = "polyflow.view.mongo", name = ["changeTrackingMode"], havingValue = "CHANGE_STREAM", matchIfMissing = false)
class DataEntryChangeTracker(
  private val dataEntryUpdateRepository: DataEntryUpdateRepository
) {

  companion object : KLogging()

  private var lastSeenResumeToken: BsonValue? = null
  private val changeStream: Flux<DataEntryDocument> = Flux
    .defer { this.dataEntryUpdateRepository.getDataEntryUpdates(lastSeenResumeToken) }
    .doOnCancel { lastSeenResumeToken = null }
    .doOnNext { event -> lastSeenResumeToken = if (event.resumeToken != null) event.resumeToken else lastSeenResumeToken }
    .doOnError(MongoCommandException::class.java) { lastSeenResumeToken = null }
    .filter { event -> filterChangeEvent(event) }
    .log(DataEntryChangeTracker::class.java.canonicalName, Level.WARNING, SignalType.ON_ERROR)
    .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100)).maxBackoff(Duration.ofSeconds(10)))
    .concatMap { event -> Mono.justOrEmpty(event.body) }
    .share()

  /**
   * Retrieves a flux of data entries updates.
   *
   * @return data entry updates stream.
   */
  fun trackDataEntryUpdates(): Flux<DataEntry> {
    return changeStream.map { it.dataEntry() }
  }

  private fun filterChangeEvent(event: ChangeStreamEvent<DataEntryDocument>): Boolean {
    return when (Objects.requireNonNull(event.operationType)) {
      OperationType.INSERT, OperationType.UPDATE, OperationType.REPLACE -> {
        logger.debug { "Got " + event.operationType + " event: " + event }
        true
      }
      else -> {
        logger.trace { "Ignoring " + event.operationType + " event: " + event }
        false
      }
    }
  }

}
