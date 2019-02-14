package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.camunda.taskpool.view.DataEntry
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


/**
 * Repository for task documents.
 */
@Repository
interface TaskRepository : MongoRepository<TaskDocument, String> {

  @Query(  "{ \$or: [{ \$or: [ { 'assignee' : ?0 }, { 'candidateUsers' : ?0 } ] }, { 'candidateGroups' : ?1} ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): List<TaskDocument>
}

