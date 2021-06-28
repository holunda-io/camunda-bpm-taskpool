package io.holunda.polyflow.view.mongo.task

import io.holunda.polyflow.view.auth.User
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
