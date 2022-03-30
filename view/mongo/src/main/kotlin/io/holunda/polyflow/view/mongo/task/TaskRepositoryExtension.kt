package io.holunda.polyflow.view.mongo.task

import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux

/**
 * Extension to allow single type for implementation.
 */
interface TaskRepositoryExtension : TaskCountByApplicationRepositoryExtension, TaskUpdatesExtension {
  /**
   * Find tasks visible to the user or one of the groups, optionally restricted by business key and priority.
   *
   * @param username    user's name
   * @param groupNames  user's groups
   * @param businessKey Business Key to restrict.
   * @param priorities  Priorities to restrict.
   * If non-null and non-empty, any of the contained priorities must match.
   * @param pageable    defines sorting and paging requirements.
   * @return Flux of matching documents.
   */
  fun findForUser(
    username: String,
    groupNames: Collection<String>,
    businessKey: String?,
    priorities: Collection<Int>?,
    pageable: Pageable?
  ): Flux<TaskDocument>
}
