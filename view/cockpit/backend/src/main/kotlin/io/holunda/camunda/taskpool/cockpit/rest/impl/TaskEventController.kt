package io.holunda.camunda.taskpool.cockpit.rest.impl

import io.holunda.camunda.taskpool.cockpit.rest.Rest
import io.holunda.camunda.taskpool.cockpit.rest.api.TaskEventsApi
import io.holunda.camunda.taskpool.cockpit.rest.mapper.TaskEventMapper
import io.holunda.camunda.taskpool.cockpit.rest.model.TaskEventDto
import io.holunda.camunda.taskpool.cockpit.service.QueryTaskEvents
import io.holunda.camunda.taskpool.cockpit.service.TaskEventsResponse
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Rest.PATH)
class TaskEventController(
  private val queryGateway: QueryGateway,
  private val mapper: TaskEventMapper
) : TaskEventsApi {

  override fun getTaskEvents(): ResponseEntity<List<TaskEventDto>> {
    val result = queryGateway.query(QueryTaskEvents(), ResponseTypes.instanceOf(TaskEventsResponse::class.java)).join()
    return ok(result.events.map { mapper.dto(it) })
  }
}
