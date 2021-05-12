package io.holunda.camunda.taskpool.view.mongo.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


/**
 * Repository for task documents.
 */
@Repository
interface TaskRepository : ReactiveMongoRepository<TaskDocument, String>, TaskRepositoryExtension {

  /**
   * Retrieves all tasks for user.
   */
  // Note: the query for _deleted not equal to true_ looks weird, but effectively means _null or false_ so it also captures old documents where _deleted_ is not set at all
  @Query("{ 'deleted': {\$ne: true}, \$or: [{ 'assignee': ?0 }, { 'candidateUsers': ?0 }, { 'candidateGroups': { \$in: ?1} } ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>, pageable: Pageable? = null): Flux<TaskDocument>

  /**
   * Retrieves all tasks for a process application.
   */
  @Query("{ 'deleted': {\$ne: true}, 'sourceReference.applicationName': ?0 }")
  fun findAllForApplication(@Param("applicationName") applicationName: String): Flux<TaskDocument>

  /**
   * Retrieves a not deleted task with given id.
   */
  @Query("{ '_id': ?0, 'deleted': {\$ne: true} }")
  fun findNotDeletedById(id: String): Mono<TaskDocument>
}

