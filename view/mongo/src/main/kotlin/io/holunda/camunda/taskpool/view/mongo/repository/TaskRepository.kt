package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.view.Task
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository


/**
 * Repository for tasks.
 */
@Repository
interface TaskRepository : ReactiveMongoRepository<Task, String> {

  // @Query("{ \$or: [ { 'assignee' : ?0 }, { 'candidateUsers' \$in ?0 }, { 'candidateGroups' \$in ?1 } ] }")
  // fun findByUser(userName: String, candidateGroups: Set<String>): Flux<Task>
}



