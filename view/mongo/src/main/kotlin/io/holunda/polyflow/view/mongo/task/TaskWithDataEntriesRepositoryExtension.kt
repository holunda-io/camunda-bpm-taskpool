package io.holunda.polyflow.view.mongo.repository

import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.mongo.service.Criterion
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux

/**
 * Repository extension.
 */
interface TaskWithDataEntriesRepositoryExtension {

  /**
   * Find all tasks with data entries matching specified filter.
   */
  fun findAllFilteredForUser(user: User, criteria: List<Criterion>, pageable: Pageable? = null): Flux<TaskWithDataEntriesDocument>
}
