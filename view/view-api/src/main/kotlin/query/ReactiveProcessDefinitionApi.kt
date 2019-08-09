package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.query.process.ProcessDefinitionsStartableByUserQuery
import java.util.concurrent.CompletableFuture

interface ReactiveProcessDefinitionApi {

  fun query(query: ProcessDefinitionsStartableByUserQuery): CompletableFuture<List<ProcessDefinition>>
}
