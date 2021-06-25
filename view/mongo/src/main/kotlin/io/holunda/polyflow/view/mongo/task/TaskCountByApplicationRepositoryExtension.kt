package io.holunda.polyflow.view.mongo.repository

import io.holunda.camunda.taskpool.view.query.task.ApplicationWithTaskCount
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Counts tasks.
 */
interface TaskCountByApplicationRepositoryExtension {
  /**
   * Retrieves counts grouped by application names.
   */
  fun findTaskCountsByApplication(): Flux<ApplicationWithTaskCount>

  /**
   * Retrieves a count of tasks for given application.
   */
  fun findTaskCountForApplication(applicationName: String): Mono<ApplicationWithTaskCount>

}

