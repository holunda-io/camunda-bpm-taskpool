package io.holunda.camunda.taskpool.view.mongo.repository

import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux


/**
 * Repository for task documents.
 */
@Repository
interface TaskRepository : ReactiveMongoRepository<TaskDocument, String> {

  @Query("{ \$or: [{ 'assignee': ?0 }, { 'candidateUsers': ?0 }, { 'candidateGroups': { \$in: ?1} } ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): Flux<TaskDocument>
}
