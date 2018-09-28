package io.holunda.camunda.taskpool.view.simple.service

import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.responsetypes.ResponseTypes
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux

data class TasksForUserQuery(
  val user: User
) {

  fun subscribeTo(queryGateway: QueryGateway) = with(queryGateway.subscriptionQuery(
      this,
      ResponseTypes.multipleInstancesOf(Task::class.java),
      ResponseTypes.instanceOf(Task::class.java))) {
      // executed after subscription is created
      // initialResult().concatWith(updates())
      initialResult()
    }
  }

