package io.holunda.polyflow.view.jpa.data

import io.holunda.camunda.taskpool.api.business.DataEntryAnonymizedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryDeletedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import org.axonframework.messaging.MetaData
import java.time.Instant

/**
 * Interface for receiving all data entry relevant events.
 */
interface DataEntryEventHandler {

  /**
   * Data entry created.
   */
  fun on(event: DataEntryCreatedEvent, metaData: MetaData, eventTimestamp: Instant)

  /**
   * Data entry updated.
   */
  fun on(event: DataEntryUpdatedEvent, metaData: MetaData, eventTimestamp: Instant)

  /**
   * Data entry deleted.
   */
  fun on(event: DataEntryDeletedEvent, metaData: MetaData)

  /**
   * Data entry anonymized.
   */
  fun on(event: DataEntryAnonymizedEvent, metaData: MetaData)
}
