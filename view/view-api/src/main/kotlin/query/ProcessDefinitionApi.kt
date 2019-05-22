package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.query.process.ProcessDefinitionsStartableByUserQuery

interface ProcessDefinitionApi {

  fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition>
}
