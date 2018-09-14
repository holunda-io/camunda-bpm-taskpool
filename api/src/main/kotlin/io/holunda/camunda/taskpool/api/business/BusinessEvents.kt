package io.holunda.camunda.taskpool.api.business

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

data class DataEntryCreatedEvent(
  override val entryType: EntryType,
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  val payload: VariableMap = Variables.createVariables()
) : DataIdentity, WithCorrelations
