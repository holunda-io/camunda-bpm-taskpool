package io.holunda.camunda.taskpool.cockpit.rest.impl

import io.holunda.camunda.taskpool.cockpit.service.QueryTaskEvents
import io.holunda.camunda.taskpool.cockpit.service.TaskEventWithMetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class ReactiveTaskController(
  private val queryGateway: QueryGateway
) {
  @GetMapping(path = ["/reactive-events"])
  fun getEventTasks(): Flux<TaskEventWithMetaData> {
    val futureResult: SubscriptionQueryResult<List<TaskEventWithMetaData>, TaskEventWithMetaData> = queryGateway.subscriptionQuery(
      QueryTaskEvents(),
      ResponseTypes.multipleInstancesOf(TaskEventWithMetaData::class.java),
      ResponseTypes.instanceOf(TaskEventWithMetaData::class.java)
    )

    val initialResult: Mono<List<TaskEventWithMetaData>> = futureResult.initialResult()
    val updates: Flux<TaskEventWithMetaData> = futureResult.updates()

    val initialAsFlux: Flux<TaskEventWithMetaData> = initialResult.flatMapMany { Flux.fromIterable(it) }

    return initialAsFlux
    // .concatWith(updates)
  }

}
