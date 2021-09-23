package io.holunda.polyflow.view.mongo.process

import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux


/**
 * Repository for process definitions.
 */
@Repository
interface ProcessDefinitionRepository : ReactiveMongoRepository<ProcessDefinitionDocument, String> {

  /**
   * Find all for given user.
   */
  @Query("{ \$or: [ { 'candidateStarterUsers' : ?0 }, { 'candidateStarterGroups' : ?1} ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): Flux<ProcessDefinitionDocument>

}

