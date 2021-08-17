package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

internal class ExtensionsTest {

  private val now = Instant.now()
  private val entry1 = ProtocolEntry(
    time = now,
    state = ProcessingType.IN_PROGRESS.of("Created"),
    username = "kermit",
    logMessage = "Created entry",
    logDetails = "Successfully created an entry"
  )
  private val entry2 = ProtocolEntry(
    time = now.plusSeconds(10),
    state = ProcessingType.IN_PROGRESS.of("Updated"),
    username = "kermit",
    logMessage = "Updated entry",
    logDetails = null
  )

  @Test
  fun `should not add protocol entry if already in the list`() {

    val protocol = listOf(
      entry1, entry2
    )

    val result = protocol.addModification(
      modification = Modification(
        time = entry1.time.atOffset(ZoneOffset.UTC),
        username = entry1.username,
        log = entry1.logMessage,
        logNotes = entry1.logDetails
      ),
      state = entry1.state
    )

    assertThat(result).isEqualTo(protocol)
  }

  @Test
  fun `should add protocol entry if not already in the list`() {

    val protocol = listOf(
      entry1, entry2
    )

    val result = protocol.addModification(
      modification = Modification(
        time = now.plusSeconds(25).atOffset(ZoneOffset.UTC),
        username = entry1.username,
        log = entry1.logMessage,
        logNotes = entry1.logDetails
      ),
      state = entry1.state
    )

    assertThat(result).isNotEqualTo(protocol)
    assertThat(result.size).isEqualTo(protocol.size + 1)
    assertThat(result).containsAll(protocol)
    assertThat(result).contains(
      ProtocolEntry(
        time = now.plusSeconds(25),
        state = entry1.state,
        username = entry1.username,
        logMessage = entry1.logMessage,
        logDetails = entry1.logDetails
      )
    )
  }
}
