package io.holunda.polyflow.view.jpa.data


import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.DataEntryAnonymizedEvent
import io.holunda.camunda.taskpool.api.business.Modification
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ConverterExtKtTest {
    @Test
    fun shouldAnonymizeDataEntry() {
        val now = OffsetDateTime.now()
        val event = DataEntryAnonymizedEvent(
            "type",
            "id",
            "ANONYMIZED",
            listOf("SYSTEM"),
            Modification(now, "SYSTEM", "anonymize", "anonymize")
        )

        val entity = DataEntryEntity(
            dataEntryId = DataEntryId("id", "type"),
            type = "type",
            name = "name",
            applicationName = "applicationName",
            state = DataEntryStateEmbeddable("IN_PROGRESS", "state"),
            authorizedPrincipals = mutableSetOf("USER:example:luffy", "GROUP:example:strawhats")
        ).also {
            it.protocol
                .addModification(it, Modification(username = "luffy"), it.state.toState())
                .addModification(it, Modification(username = "zoro"), it.state.toState())
                .addModification(it, Modification(username = "SYSTEM"), it.state.toState())
        }

        val anonymized = event.toEntity(RevisionValue.NO_REVISION, entity)

        assertThat(anonymized.authorizedPrincipals).hasSize(1)
        assertThat(anonymized.authorizedPrincipals).allMatch { it.startsWith("GROUP") }
        assertThat(anonymized.protocol).extracting(ProtocolElement::username)
            .containsExactly(tuple("ANONYMIZED"), tuple("ANONYMIZED"), tuple("SYSTEM"), tuple("SYSTEM"))
    }
}