package io.holunda.camunda.taskpool.api.business

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