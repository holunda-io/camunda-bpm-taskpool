package io.holunda.polyflow.datapool.core

import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.core.data-entry")
data class DataPoolProperties(
  /**
   * How many events lead to a snapshot of data entries. Check the [eventSourcingRepositoryType] property.
   */
  val snapshotThreshold: Int = 5,

  /**
   * Specifies the full-qualified class name of the event sourced repository used to load data entry aggregates.
   * Defaults to [org.axonframework.eventsourcing.EventSourcingRepository].
   * Consider to use [io.holunda.polyflow.datapool.core.repository.FirstEventOnlyEventSourcingRepository] if you want to load the aggregate with first event only.
   */
  val eventSourcingRepositoryType: String = EventSourcingRepository::class.java.canonicalName
)