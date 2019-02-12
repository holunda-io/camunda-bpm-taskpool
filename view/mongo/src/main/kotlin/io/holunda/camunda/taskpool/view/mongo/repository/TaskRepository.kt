package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.view.Task
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux


/**
 * Repository for tasks.
 */
@Repository
interface TaskRepository : ReactiveMongoRepository<Task, String> {

  // @Query("{ \$or: [ { 'assignee' : ?0 }, { 'candidateUsers' \$in ?0 }, { 'candidateGroups' \$in ?1 } ] }")
  @Suppress("unused")
  fun findByAssignee(assignee: String): Flux<Task>
}



