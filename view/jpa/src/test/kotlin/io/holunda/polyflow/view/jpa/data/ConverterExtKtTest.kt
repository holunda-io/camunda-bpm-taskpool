package io.holunda.polyflow.view.jpa.data


import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

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

  @Test
  fun shouldCreateEntity() {
    val objectMapper = ObjectMapper()
    val event = DataEntryCreatedEvent(
      entryType = "io.holunda.test",
      entryId = "id",
      type = "type",
      applicationName = "applicationName",
      name = "name",
      correlations = Variables.putValue("io.holunda.test", "id-1"),
      payload = Variables.putValue("key", "value"),
      description = "description",
      state = ProcessingType.IN_PROGRESS.of("IN_CREATION"),
      createModification = Modification(time = OffsetDateTime.now(), "Test-User", "Created Event"),
      authorizations = listOf(AuthorizationChange.addUser("Test-User")),
      formKey = "test-entry-form"
    )

    val entity = event.toEntity(objectMapper, Instant.now(), RevisionValue.NO_REVISION, 2, listOf())

    assertThat(entity.dataEntryId.entryId).isEqualTo("id")
    assertThat(entity.dataEntryId.entryType).isEqualTo("io.holunda.test")
    assertThat(entity.type).isEqualTo("type")
    assertThat(entity.applicationName).isEqualTo("applicationName")
    assertThat(entity.name).isEqualTo("name")
    assertThat(entity.correlations).containsExactly(DataEntryId("id-1", "io.holunda.test"))
    assertThat(entity.payload).isEqualTo("""{"key":"value"}""")
    assertThat(entity.payloadAttributes).containsExactly(PayloadAttribute("key", "value"))
    assertThat(entity.description).isEqualTo("description")
    assertThat(entity.revision).isEqualTo(0L)
    assertThat(entity.protocol).hasSize(1)
    assertThat(entity.formKey).isEqualTo("test-entry-form")
    assertThat(entity.authorizedPrincipals).containsExactly("USER:Test-User")
    assertThat(entity.state.processingType).isEqualTo("IN_PROGRESS")
    assertThat(entity.state.state).isEqualTo("IN_CREATION")
  }

  @Test
  fun shouldCreateEntityIfNotPresent() {
    val objectMapper = ObjectMapper()
    val event = DataEntryUpdatedEvent(
      entryType = "io.holunda.test",
      entryId = "id",
      type = "type",
      applicationName = "applicationName",
      name = "name",
      correlations = Variables.putValue("io.holunda.test", "id-1"),
      payload = Variables.putValue("key", "value"),
      description = "description",
      state = ProcessingType.IN_PROGRESS.of("IN_CREATION"),
      updateModification = Modification(time = OffsetDateTime.now(), "Test-User", "Created Event"),
      authorizations = listOf(AuthorizationChange.addUser("Test-User")),
      formKey = "test-entry-form"
    )

    val entity = event.toEntity(
      objectMapper,
      Instant.now(),
      RevisionValue.NO_REVISION,
      null,
      2,
      listOf()
    )

    assertThat(entity.dataEntryId.entryId).isEqualTo("id")
    assertThat(entity.dataEntryId.entryType).isEqualTo("io.holunda.test")
    assertThat(entity.type).isEqualTo("type")
    assertThat(entity.applicationName).isEqualTo("applicationName")
    assertThat(entity.name).isEqualTo("name")
    assertThat(entity.correlations).containsExactly(DataEntryId("id-1", "io.holunda.test"))
    assertThat(entity.payload).isEqualTo("""{"key":"value"}""")
    assertThat(entity.payloadAttributes).containsExactly(PayloadAttribute("key", "value"))
    assertThat(entity.description).isEqualTo("description")
    assertThat(entity.revision).isEqualTo(0L)
    assertThat(entity.protocol).hasSize(1)
    assertThat(entity.formKey).isEqualTo("test-entry-form")
    assertThat(entity.authorizedPrincipals).containsExactly("USER:Test-User")
    assertThat(entity.state.processingType).isEqualTo("IN_PROGRESS")
    assertThat(entity.state.state).isEqualTo("IN_CREATION")
  }

  @Test
  fun shouldUpdateEntity() {
    val objectMapper = ObjectMapper()
    val createdAt = Instant.now().minus(1, ChronoUnit.DAYS)
    val existingEntity = DataEntryEntity(
      dataEntryId = DataEntryId("id", "io.holunda.test"),
      type = "type",
      name = "name",
      applicationName = "applicationName",
      formKey = "test-entry-form",
      revision = 0L,
      state = DataEntryStateEmbeddable("IN_PROGRESS", "IN_CREATION"),
      description = "description",
      createdDate = createdAt,
      lastModifiedDate = createdAt,
      deletedDate = null,
      authorizedPrincipals = mutableSetOf("USER:Test-User"),
      payloadAttributes = mutableSetOf(PayloadAttribute("key", "value")),
      protocol = mutableListOf(),
      payload = """{"key":"value"}""",
      correlations = mutableSetOf(DataEntryId("id-1","io.holunda.test", )),
    )

    existingEntity.protocol.add(ProtocolElement(state = DataEntryStateEmbeddable("IN_PROGRESS", "IN_CREATION"), logMessage = "Created Event", dataEntry = existingEntity, username = "Test-User"))

    val event = DataEntryUpdatedEvent(
      entryType = "io.holunda.test",
      entryId = "id",
      type = "type",
      applicationName = "applicationName",
      name = "name",
      correlations = Variables.putValue("io.holunda.test", "id-2"),
      payload = Variables.putValue("key", "value").putValue("key-1", "value-1"),
      description = "description",
      state = ProcessingType.IN_PROGRESS.of("IN_CREATION"),
      updateModification = Modification(time = OffsetDateTime.now(), "Test-User", "Created Event"),
      authorizations = listOf(AuthorizationChange.addGroup("Test-Group"), AuthorizationChange.addUser("Test-User-1"), AuthorizationChange.removeUser("Test-User")),
      formKey = "test-entry-form"
    )

    val entity = event.toEntity(
      objectMapper,
      Instant.now(),
      RevisionValue.NO_REVISION,
      null,
      2,
      listOf()
    )

    assertThat(entity.dataEntryId.entryId).isEqualTo("id")
    assertThat(entity.dataEntryId.entryType).isEqualTo("io.holunda.test")
    assertThat(entity.type).isEqualTo("type")
    assertThat(entity.applicationName).isEqualTo("applicationName")
    assertThat(entity.name).isEqualTo("name")
    assertThat(entity.correlations).containsExactlyInAnyOrder(DataEntryId("id-2", "io.holunda.test"))
    assertThat(entity.payload).isEqualTo("""{"key-1":"value-1","key":"value"}""")
    assertThat(entity.payloadAttributes).containsExactlyInAnyOrder(PayloadAttribute("key-1", "value-1"), PayloadAttribute("key", "value"))
    assertThat(entity.description).isEqualTo("description")
    assertThat(entity.revision).isEqualTo(0L)
    assertThat(entity.protocol).hasSize(1)
    assertThat(entity.formKey).isEqualTo("test-entry-form")
    assertThat(entity.authorizedPrincipals).containsExactlyInAnyOrder("GROUP:Test-Group", "USER:Test-User-1")
    assertThat(entity.state.processingType).isEqualTo("IN_PROGRESS")
    assertThat(entity.state.state).isEqualTo("IN_CREATION")
  }
}
