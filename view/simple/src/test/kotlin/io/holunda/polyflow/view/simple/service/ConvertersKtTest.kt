package io.holunda.polyflow.view.simple.service

import io.holunda.camunda.taskpool.api.business.DataEntryAnonymizedEvent
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.addModification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ConvertersKtTest {
    @Test
    fun `should apply anonymization`() {
        val now = OffsetDateTime.now()
        val event = DataEntryAnonymizedEvent(
            "type", "id", "ANONYMIZED", listOf("SYSTEM"), Modification(now, "SYSTEM", "anonymize", "anonymize")
        )

        val dataEntry = DataEntry(
            entryId = "id",
            entryType = "type",
            applicationName = "application name",
            name = "name",
            type = "type",
            authorizedGroups = setOf("strawhats"),
            authorizedUsers = setOf("luffy")
        ).also {
            it.protocol.addModification(Modification(username = "luffy"), it.state)
            it.protocol.addModification(Modification(username = "zoro"), it.state)
            it.protocol.addModification(Modification(username = "SYSTEM"), it.state)
        }

        val anonymized = event.toDataEntry(dataEntry)

        assertThat(anonymized.authorizedGroups).containsExactly("strawhats")
        assertThat(anonymized.authorizedUsers).isEmpty()
        assertThat(anonymized.protocol).allMatch { it.username in listOf("SYSTEM", "ANONYMIZED") }
    }
}