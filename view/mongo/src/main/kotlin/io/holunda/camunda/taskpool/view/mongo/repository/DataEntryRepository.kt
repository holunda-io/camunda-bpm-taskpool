package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Repository for data entry documents.
 */
@Repository
interface DataEntryRepository : ReactiveMongoRepository<DataEntryDocument, String> {

  /**
   * Reactive finder by identity.
   */
  fun findByIdentity(identity: DataIdentity): Mono<DataEntryDocument> = findById(dataIdentityString(entryId = identity.entryId, entryType = identity.entryType))

  /**
   * Reactive finder by type.
   */
  fun findAllByEntryType(entryType: EntryType): Flux<DataEntryDocument>

  /**
   * Finder for user.
   */
  @Query("{ \$or: [ { 'authorizedUsers' : ?0 }, { 'authorizedGroups' : { \$in: ?1 } } ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): Flux<DataEntryDocument>
}
