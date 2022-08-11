package io.holunda.polyflow.datapool.core.business.repository

import io.holunda.polyflow.datapool.core.business.DataEntryAggregate
import io.holunda.polyflow.datapool.core.repository.FirstEventOnlyEventSourcingRepositoryBuilder
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventsourcing.NoSnapshotTriggerDefinition
import org.axonframework.eventsourcing.eventstore.EventStore
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class FirstEventOnlyEventSourcingRepositoryBuilderTest {

  private val eventStore: EventStore = mock()

  @Test
  fun `should construct the builder with defaults`() {
    val builder = FirstEventOnlyEventSourcingRepositoryBuilder(DataEntryAggregate::class.java).eventStore(eventStore)

    assertThat(builder.internalEventStore()).isEqualTo(eventStore)
    assertThat(builder.internalSnapshotTriggerDefinition()).isEqualTo(NoSnapshotTriggerDefinition.INSTANCE)
    assertThat(builder.internalRepositoryProvider()).isNull()
    assertThat(builder.internalEventStreamFilter()).isNull()
  }
}