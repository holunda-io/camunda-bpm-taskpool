package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.ProcessDefinition

interface ProcessDefinitionApi {

  fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition>
}
