package io.holunda.camunda.taskpool.cockpit.rest.impl

import io.holunda.camunda.taskpool.cockpit.rest.Rest
import io.holunda.camunda.taskpool.cockpit.rest.mapper.TaskEventMapper
import io.holunda.camunda.taskpool.cockpit.rest.model.TaskEventDto
import io.holunda.camunda.taskpool.cockpit.service.QueryTaskEvents
import io.holunda.camunda.taskpool.cockpit.service.TaskEventWithMetaData
import mu.KLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

/**
 * Task event controller.
 */
@RestController
@RequestMapping(path = [Rest.PATH])
class CockpitTasksController(
  private val queryGateway: QueryGateway,
  private val mapper: TaskEventMapper
) {

  companion object : KLogging()

  /**
   * Retrieves all task events.
   */
  @GetMapping(path = ["/task-events"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE])
  fun getEventTasks(): Flux<TaskEventDto> {

    val taskEvents: SubscriptionQueryResult<List<TaskEventWithMetaData>, TaskEventWithMetaData> = queryGateway.subscriptionQuery(
      QueryTaskEvents(),
      ResponseTypes.multipleInstancesOf(TaskEventWithMetaData::class.java),
      ResponseTypes.instanceOf(TaskEventWithMetaData::class.java)
    )

    return taskEvents
      .initialResult()
      .flatMapMany { Flux.fromIterable(it) }
      .concatWith(taskEvents.updates())
      .map {
        mapper.dto(it)
      }
  }
}
