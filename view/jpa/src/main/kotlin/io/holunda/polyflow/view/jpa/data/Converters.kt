package io.holunda.polyflow.view.jpa.data

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.ProtocolEntry
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipalType
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables

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
    payload = this.payload.toPayloadVariableMap(objectMapper)
  )

/**
 * Retrieves user authorizations as list of user names.
 */
fun Set<AuthorizationPrincipal>.asUsernames() = this.filter { it.id.type == AuthorizationPrincipalType.USER }.map { it.id.name }.toSet()

/**
 * Retrieves group authorizations as list of group names.
 */
fun Set<AuthorizationPrincipal>.asGroupnames() = this.filter { it.id.type == AuthorizationPrincipalType.GROUP }.map { it.id.name }.toSet()

/**
 * Serializes payload as JSON.
 */
fun VariableMap.toPayloadJson(objectMapper: ObjectMapper): String =
  objectMapper.writeValueAsString(this)

/**
 * Deserializes JSON back into variable map.
 */
fun String?.toPayloadVariableMap(objectMapper: ObjectMapper): VariableMap = if (this != null) {
  val mapType = objectMapper.typeFactory.constructMapType(Map::class.java, String::class.java, Any::class.java)
  val map: Map<String, Any> = objectMapper.convertValue(this, mapType)
  createVariables().apply {
    putAll(map)
  }
} else {
  createVariables()
}

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
fun DataEntryCreatedEvent.toEntity(objectMapper: ObjectMapper, revisionValue: RevisionValue) = DataEntryEntity(
  dataEntryId = DataEntryId(entryType = this.entryType, entryId = this.entryId),
  payload = this.payload.toPayloadJson(objectMapper),
  name = this.name,
  applicationName = this.applicationName,
  type = this.type,
  description = this.description,
  state = this.state.toState(),
  formKey = this.formKey,
  revision = if (revisionValue != RevisionValue.NO_REVISION) {
    revisionValue.revision
  } else {
    0L
  },
  authorizedPrincipals = AuthorizationChange.applyUserAuthorization(setOf(), this.authorizations).map { AuthorizationPrincipal.user(it) }
    .plus(AuthorizationChange.applyGroupAuthorization(setOf(), this.authorizations).map { AuthorizationPrincipal.group(it) }).toSet(),
).apply {
  this.protocol = this.protocol.addModification(this, this@toEntity.createModification, this@toEntity.state)
}

/**
 * Event to entity for an update, if an optional entry exists.
 */
fun DataEntryUpdatedEvent.toEntity(objectMapper: ObjectMapper, revisionValue: RevisionValue, oldEntry: DataEntryEntity?) = if (oldEntry == null) {
  DataEntryEntity(
    dataEntryId = DataEntryId(entryType = this.entryType, entryId = this.entryId),
    payload = this.payload.toPayloadJson(objectMapper),
    name = this.name,
    applicationName = this.applicationName,
    type = this.type,
    description = this.description,
    state = this.state.toState(),
    formKey = this.formKey,
    authorizedPrincipals = AuthorizationChange.applyUserAuthorization(setOf(), this.authorizations).map { AuthorizationPrincipal.user(it) }
      .plus(AuthorizationChange.applyGroupAuthorization(setOf(), this.authorizations).map { AuthorizationPrincipal.group(it) }).toSet(),
    revision = if (revisionValue != RevisionValue.NO_REVISION) {
      revisionValue.revision
    } else {
      0L
    },
  )
} else {
  oldEntry.also {
    it.payload = this.payload.toPayloadJson(objectMapper)
    it.name = this.name
    it.applicationName = this.applicationName
    it.type = this.type
    it.description = this.description
    it.state = this.state.toState()
    it.formKey = this.formKey
    it.authorizedPrincipals =
      AuthorizationChange.applyUserAuthorization(
        it.authorizedPrincipals.asUsernames(),
        this.authorizations
      ).map { AuthorizationPrincipal.user(it) }
        .plus(AuthorizationChange.applyGroupAuthorization(
          it.authorizedPrincipals.asGroupnames(),
          this.authorizations
        ).map { AuthorizationPrincipal.group(it) })
        .toSet()
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
 * Adds a modification to the protocol.
 */
fun List<ProtocolElement>.addModification(dataEntry: DataEntryEntity, modification: Modification, state: DataEntryState) =
  this.plus(
    ProtocolElement(
      dataEntry = dataEntry,
      time = modification.time.toInstant(),
      username = modification.username,
      logMessage = modification.log,
      logDetails = modification.logNotes,
      state = state.toState()
    )
  )
