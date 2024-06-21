package io.holunda.camunda.taskpool.api.business

/**
 * Maps command to event.
 */
fun CreateDataEntryCommand.createdEvent() = DataEntryCreatedEvent(
  entryId = this.dataEntryChange.entryId,
  entryType = this.dataEntryChange.entryType,
  name = this.dataEntryChange.name,
  type = this.dataEntryChange.type,
  applicationName = this.dataEntryChange.applicationName,
  state = this.dataEntryChange.state,
  description = this.dataEntryChange.description,
  payload = this.dataEntryChange.payload,
  correlations = this.dataEntryChange.correlations,
  createModification = this.dataEntryChange.modification,
  authorizations = this.dataEntryChange.authorizationChanges,
  formKey = this.dataEntryChange.formKey
)

/**
 * Maps command to event.
 */
fun UpdateDataEntryCommand.updatedEvent() = DataEntryUpdatedEvent(
  entryId = this.dataEntryChange.entryId,
  entryType = this.dataEntryChange.entryType,
  name = this.dataEntryChange.name,
  type = this.dataEntryChange.type,
  applicationName = this.dataEntryChange.applicationName,
  state = this.dataEntryChange.state,
  description = this.dataEntryChange.description,
  payload = this.dataEntryChange.payload,
  correlations = this.dataEntryChange.correlations,
  updateModification = this.dataEntryChange.modification,
  authorizations = this.dataEntryChange.authorizationChanges,
  formKey = this.dataEntryChange.formKey
)

/**
 * Maps command to event.
 */
fun DeleteDataEntryCommand.deletedEvent() = DataEntryDeletedEvent(
  entryId = this.entryId,
  entryType = this.entryType,
  deleteModification = this.modification,
  state = this.state
)

/**
 * Maps command to event.
 */
fun AnonymizeDataEntryCommand.anonymizeEvent() = DataEntryAnonymizedEvent(
  entryId = this.entryId,
  entryType = this.entryType,
  type = this.type,
  anonymizedUsername = this.anonymizedUsername,
  excludedUsernames = this.excludedUsernames,
  anonymizeModification = this.anonymizeModification
)