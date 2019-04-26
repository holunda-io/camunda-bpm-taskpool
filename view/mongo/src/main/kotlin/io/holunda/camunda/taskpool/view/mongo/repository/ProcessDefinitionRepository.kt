package io.holunda.camunda.taskpool.view.mongo.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


/**
 * Repository for process definitions.
 */
@Repository
interface ProcessDefinitionRepository : MongoRepository<ProcessDefinitionDocument, String> {

  @Query("{ \$or: [ { 'candidateStarterUsers' : ?0 }, { 'candidateStarterGroups' : ?1} ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): List<ProcessDefinitionDocument>

}

