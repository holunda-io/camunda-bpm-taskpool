package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

data class TestDataEntry(
  val entryType: EntryType = "test-type",
  val entryId: EntryId,
  val type: String = "Test Type",
  val applicationName: String = "test-application",
  val name: String,
  val correlations: CorrelationMap = newCorrelations(),
  val payload: VariableMap = Variables.createVariables().putValue("my-property", "myValue").putValue("entryId", entryId),
  val description: String? = null,
  val state: DataEntryState = ProcessingType.UNDEFINED.of(""),
  val createModification: Modification = Modification.now(),
  val updateModification: Modification = Modification.now(),
  val authorizations: List<AuthorizationChange> = listOf(AuthorizationChange.addUser("kermit")),
  val formKey: String? = null
) {

  fun asCreatedEvent() = DataEntryCreatedEvent(
    entryType = this.entryType,
    entryId = this.entryId,
    type = this.type,
    applicationName = this.applicationName,
    name = this.name,
    correlations = this.correlations,
    payload = this.payload,
    description = this.description,
    state = this.state,
    createModification = this.createModification,
    authorizations = this.authorizations,
    formKey = this.formKey
  )

  fun asUpdatedEvent() = DataEntryUpdatedEvent(
    entryType = this.entryType,
    entryId = this.entryId,
    type = this.type,
    applicationName = this.applicationName,
    name = this.name,
    correlations = this.correlations,
    payload = this.payload,
    description = this.description,
    state = this.state,
    updateModification = this.updateModification,
    authorizations = this.authorizations,
    formKey = this.formKey
  )

  fun asDataEntry() = asCreatedEvent().toDataEntry()

}
