package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.ProcessesApi
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.ProcessDefinitionMapper
import io.holunda.camunda.taskpool.example.tasklist.rest.model.ProcessDefinitionDto
import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.auth.UserService
import io.holunda.camunda.taskpool.view.query.ProcessDefinitionsStartableByUserQuery
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(Rest.REQUEST_PATH)
class StartableProcessDefinitionController(
  private val currentUserService: CurrentUserService,
  private val userService: UserService,
  private val queryGateway: QueryGateway,
  private val mapper: ProcessDefinitionMapper
) : ProcessesApi {

  override fun getStartableProcesses(): ResponseEntity<List<ProcessDefinitionDto>> {

    val username = currentUserService.getCurrentUser()
    val user = userService.getUser(username)

    val result: List<ProcessDefinition> = queryGateway
      .query(ProcessDefinitionsStartableByUserQuery(user = user), ResponseTypes.multipleInstancesOf(ProcessDefinition::class.java))
      .join()

    return ok()
      .body(result.map { mapper.dto(it) })

  }
}
