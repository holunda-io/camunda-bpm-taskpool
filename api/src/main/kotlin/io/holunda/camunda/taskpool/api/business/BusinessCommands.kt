package io.holunda.camunda.taskpool.api.business

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

data class CreateDataEntryCommand(
  override val entryType: EntryType,
  @TargetAggregateIdentifier
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  val payload: VariableMap = Variables.createVariables()
): DataIdentity, WithCorrelations
