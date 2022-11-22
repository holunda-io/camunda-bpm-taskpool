package io.holunda.camunda.taskpool.api.business

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ProcessingTypeTest {

  @Test
  fun `creates processing type`() {
    assertThat(ProcessingType.PRELIMINARY.of("draft")).isEqualTo(DataEntryStateImpl(processingType = ProcessingType.PRELIMINARY, state = "draft"))
    assertThat(ProcessingType.IN_PROGRESS.of("doing")).isEqualTo(DataEntryStateImpl(processingType = ProcessingType.IN_PROGRESS, state = "doing"))
    assertThat(ProcessingType.COMPLETED.of("done")).isEqualTo(DataEntryStateImpl(processingType = ProcessingType.COMPLETED, state = "done"))
    assertThat(ProcessingType.DELETED.of("thrashed")).isEqualTo(DataEntryStateImpl(processingType = ProcessingType.DELETED, state = "thrashed"))
    assertThat(ProcessingType.CANCELLED.of("rejected")).isEqualTo(DataEntryStateImpl(processingType = ProcessingType.CANCELLED, state = "rejected"))
    assertThat(ProcessingType.UNDEFINED.of("custom")).isEqualTo(DataEntryStateImpl(processingType = ProcessingType.UNDEFINED, state = "custom"))
  }
}