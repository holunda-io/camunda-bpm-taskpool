package io.holunda.polyflow.view.jpa.data

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import org.axonframework.messaging.MetaData

/**
 * Interface for receiving all data entry relevant events.
 */
interface DataEntryEventHandler {

  /**
   * Data entry created.
   */
  fun on(event: DataEntryCreatedEvent, metaData: MetaData)

  /**
   * Data entry updated.
   */
  fun on(event: DataEntryUpdatedEvent, metaData: MetaData)
}
