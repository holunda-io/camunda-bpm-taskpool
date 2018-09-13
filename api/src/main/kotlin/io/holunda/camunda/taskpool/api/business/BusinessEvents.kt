package io.holunda.camunda.taskpool.api.business

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

data class DataEntryCreatedEvent(
  val entryType: String,
  val entryId: String,
  val payload: VariableMap = Variables.createVariables()
)
