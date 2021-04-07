package io.holunda.camunda.taskpool.view.query.process

data class ProcessVariablesForInstanceQuery(
  val processInstanceId: String,
  val variableFilter: List<ProcessVariableFilter>
)
