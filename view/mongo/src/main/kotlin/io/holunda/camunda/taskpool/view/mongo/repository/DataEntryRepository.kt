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

  fun findByIdentity(identity: DataIdentity): Mono<DataEntryDocument> = findById(dataIdentityString(entryId = identity.entryId, entryType = identity.entryType))
  fun findAllByEntryType(entryType: EntryType): Flux<DataEntryDocument>

  @Query("{ \$or: [ { 'authorizedUsers' : ?0 }, { 'authorizedGroups' : { \$in: ?1 } } ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): Flux<DataEntryDocument>

}
