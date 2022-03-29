package io.holunda.polyflow.view.mongo.data

import com.mongodb.MongoCommandException
import io.holunda.polyflow.view.DataEntry
import mu.KLogging
import org.bson.BsonValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.util.retry.Retry
import java.time.Duration
import java.util.logging.Level

/**
 * Tracks changes of data entries.
 * Only active if `polyflow.view.mongo.changeTrackingMode` is set to `CHANGE_STREAM`.
 */
@Component
@ConditionalOnProperty(prefix = "polyflow.view.mongo", name = ["changeTrackingMode"], havingValue = "CHANGE_STREAM", matchIfMissing = false)
class DataEntryChangeTracker(
  private val dataEntryRepository: DataEntryRepository
) {

  companion object : KLogging()

  private var lastSeenResumeToken: BsonValue? = null

  private val changeStream: Flux<DataEntry> = Flux
    .defer { this.dataEntryRepository.getDataEntryUpdates(lastSeenResumeToken) }
    .doOnCancel { lastSeenResumeToken = null }
    .doOnNext { event -> lastSeenResumeToken = if (event.resumeToken != null) event.resumeToken else lastSeenResumeToken }
    .doOnError(MongoCommandException::class.java) { lastSeenResumeToken = null }
    .doOnNext { event -> logger.debug { "Got event: $event" } }
    .log(DataEntryChangeTracker::class.java.canonicalName, Level.WARNING, SignalType.ON_ERROR)
    .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100)).maxBackoff(Duration.ofSeconds(10)))
    .concatMap { event -> Mono.justOrEmpty(event.body) }
    .map { it.dataEntry() }
    .share()

  /**
   * Retrieves a flux of data entries updates.
   *
   * @return data entry updates stream.
   */
  fun trackDataEntryUpdates(): Flux<DataEntry> = changeStream
}
