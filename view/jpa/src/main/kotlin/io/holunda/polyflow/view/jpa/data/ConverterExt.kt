package io.holunda.polyflow.view.jpa.data

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.variable.serializer.*
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.ProtocolEntry
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipalType.GROUP
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipalType.USER
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import org.camunda.bpm.engine.variable.Variables
import java.time.Instant

/**
 * Converts the entity into API type.
 */
fun DataEntryEntity.toDataEntry(objectMapper: ObjectMapper) =
  DataEntry(
    entryType = this.dataEntryId.entryType,
    entryId = this.dataEntryId.entryId,
    type = this.type,
    name = this.name,
    description = this.description,
    applicationName = this.applicationName,
    formKey = this.formKey,
    state = this.state.toState(),
    protocol = this.protocol.map { it.toProtocolEntry() },
    authorizedUsers = this.authorizedPrincipals.asUsernames(),
    authorizedGroups = this.authorizedPrincipals.asGroupnames(),
    payload = this.payload.toPayloadVariableMap(objectMapper),
    correlations = Variables.fromMap(this.correlations.associate { it.entryType to it.entryId })
  )

/**
 * Retrieves user authorizations as list of user names.
 */
fun Set<String>.asUsernames() = this.filter { AuthorizationPrincipal(it).type == USER }.map { AuthorizationPrincipal(it).name }.toSet()

/**
 * Retrieves group authorizations as list of group names.
 */
fun Set<String>.asGroupnames() = this.filter { AuthorizationPrincipal(it).type == GROUP }.map { AuthorizationPrincipal(it).name }.toSet()

/**
 * Converts entity to API type.
 */
fun ProtocolElement.toProtocolEntry() =
  ProtocolEntry(
    time = this.time,
    username = this.username,
    state = this.state.toState(),
    logMessage = this.logMessage,
    logDetails = this.logDetails
  )

/**
 * Converts stored value into API type.
 */
fun DataEntryStateEmbeddable.toState(): DataEntryState = ProcessingType.valueOf(this.processingType).of(this.state)

/**
 * Converts API state into persistence format.
 */
fun DataEntryState.toState() = DataEntryStateEmbeddable(processingType = this.processingType.name, state = this.state ?: "")

/**
 * Event to entity.
 */
fun DataEntryCreatedEvent.toEntity(
  objectMapper: ObjectMapper, 
  eventTimestamp: Instant, 
  revisionValue: RevisionValue, 
  limit: Int, 
  filters: List<Pair<JsonPathFilterFunction, FilterType>>,
  payLoadAttributeColumnLength: Int? = null
) = DataEntryEntity(
  dataEntryId = DataEntryId(entryType = this.entryType, entryId = this.entryId),
  payload = this.payload.toPayloadJson(objectMapper),
  payloadAttributes = this.payload.toJsonPathsWithValues(limit, filters, payLoadAttributeColumnLength).map { attr -> PayloadAttribute(attr) }.toMutableSet(),
  name = this.name,
  applicationName = this.applicationName,
  type = this.type,
  createdDate = this.createModification.time.toInstant(),
  lastModifiedDate = this.createModification.time.toInstant(),
  description = this.description,
  state = this.state.toState(),
  formKey = this.formKey,
  revision = if (revisionValue != RevisionValue.NO_REVISION) {
    revisionValue.revision
  } else {
    0L
  },
  versionTimestamp = eventTimestamp.toEpochMilli(),
  authorizedPrincipals = AuthorizationChange.applyUserAuthorization(mutableSetOf(), this.authorizations).map { user(it).toString() }
    .plus(AuthorizationChange.applyGroupAuthorization(mutableSetOf(), this.authorizations).map { group(it).toString() }).toMutableSet(),
  correlations = this.correlations.toMutableMap().map { entry -> DataEntryId(entryType = entry.key, entryId = entry.value.toString()) }.toMutableSet()
).apply {
  this.protocol = this.protocol.addModification(this, this@toEntity.createModification, this@toEntity.state)
}

/**
 * Event to entity for an update, if an optional entry exists.
 */
fun DataEntryUpdatedEvent.toEntity(
  objectMapper: ObjectMapper,
  eventTimestamp: Instant,
  revisionValue: RevisionValue,
  oldEntry: DataEntryEntity?,
  limit: Int,
  filters: List<Pair<JsonPathFilterFunction, FilterType>>,
  payLoadAttributeColumnLength: Int? = null
) = if (oldEntry == null) {
  DataEntryEntity(
    dataEntryId = DataEntryId(entryType = this.entryType, entryId = this.entryId),
    payload = this.payload.toPayloadJson(objectMapper),
    payloadAttributes = this.payload.toJsonPathsWithValues(limit, filters, payLoadAttributeColumnLength).map { attr -> PayloadAttribute(attr) }.toMutableSet(),
    name = this.name,
    applicationName = this.applicationName,
    type = this.type,
    createdDate = this.updateModification.time.toInstant(),
    lastModifiedDate = this.updateModification.time.toInstant(),
    description = this.description,
    state = this.state.toState(),
    formKey = this.formKey,
    authorizedPrincipals = AuthorizationChange.applyUserAuthorization(setOf(), this.authorizations).map { user(it).toString() }
      .plus(AuthorizationChange.applyGroupAuthorization(setOf(), this.authorizations).map { group(it).toString() }).toMutableSet(),
    correlations = this.correlations.toMutableMap().map { entry -> DataEntryId(entryType = entry.key, entryId =  entry.value.toString()) }.toMutableSet(),
    revision = if (revisionValue != RevisionValue.NO_REVISION) {
      revisionValue.revision
    } else {
      0L
    },
    versionTimestamp = eventTimestamp.toEpochMilli(),
  )
} else {
  oldEntry.also {
    it.payload = this.payload.toPayloadJson(objectMapper)
    it.payloadAttributes = this.payload.toJsonPathsWithValues(limit, filters).map { attr -> PayloadAttribute(attr) }.toMutableSet()
    it.name = this.name
    it.applicationName = this.applicationName
    it.type = this.type
    it.description = this.description
    it.state = this.state.toState()
    it.lastModifiedDate = this.updateModification.time.toInstant()
    it.formKey = this.formKey
    it.authorizedPrincipals =
      AuthorizationChange.applyUserAuthorization(
        it.authorizedPrincipals.asUsernames(),
        this.authorizations
      ).map { user -> user(user).toString() }
        .plus(AuthorizationChange.applyGroupAuthorization(
          it.authorizedPrincipals.asGroupnames(),
          this.authorizations
        ).map { group -> group(group).toString() })
        .toMutableSet()
    it.correlations = this.correlations.toMutableMap().map { entry -> DataEntryId(entryType = entry.key, entryId =  entry.value.toString()) }.toMutableSet()
    it.revision = if (revisionValue != RevisionValue.NO_REVISION) {
      revisionValue.revision
    } else {
      it.revision
    }
  }
}.apply {
  this.protocol = this.protocol.addModification(this, this@toEntity.updateModification, this@toEntity.state)
}

/**
 * Adds a modification to the protocol, if it doesn't exist in the protocol already, comparing all protocol element properties
 * besides the technical id.
 */
fun MutableList<ProtocolElement>.addModification(dataEntry: DataEntryEntity, modification: Modification, state: DataEntryState) =
  ProtocolElement(
    dataEntry = dataEntry,
    time = modification.time.toInstant(),
    username = modification.username,
    logMessage = modification.log,
    logDetails = modification.logNotes,
    state = state.toState()
  ).let { protocolElement ->
    this.apply {
      if (dataEntry.protocol.none { existing -> existing.same(protocolElement) }) {
        this.add(protocolElement)
      }
    }
  }

/**
 * Creates an entity marked as deleted.
 */
fun DataEntryDeletedEvent.toEntity(
  revisionValue: RevisionValue,
  oldEntry: DataEntryEntity,
): DataEntryEntity = oldEntry.also {
  it.state = this.state.toState()
  it.lastModifiedDate = this.deleteModification.time.toInstant()
  it.deletedDate = this.deleteModification.time.toInstant()
  it.revision = if (revisionValue != RevisionValue.NO_REVISION) {
    revisionValue.revision
  } else {
    it.revision
  }
}.apply {
  this.protocol = this.protocol.addModification(this, this@toEntity.deleteModification, this@toEntity.state)
}

/**
 * Event to entity for an anonymization, if an optional entry exists.
 */
fun DataEntryAnonymizedEvent.toEntity(
  revisionValue: RevisionValue,
  oldEntry: DataEntryEntity,
) = oldEntry.also {
  it.protocol.forEach { protocolEntry ->
    if (protocolEntry.username != null && !this.excludedUsernames.contains(protocolEntry.username))
      protocolEntry.username = this.anonymizedUsername
  }
  it.authorizedPrincipals =
    it.authorizedPrincipals.filter { principal -> !principal.startsWith("USER:") }.toMutableSet()
  it.lastModifiedDate = this.anonymizeModification.time.toInstant()
  it.revision = if (revisionValue != RevisionValue.NO_REVISION) {
    revisionValue.revision
  } else {
    it.revision
  }
}.apply {
  this.protocol = this.protocol.addModification(this, this@toEntity.anonymizeModification, this.state.toState())
}
