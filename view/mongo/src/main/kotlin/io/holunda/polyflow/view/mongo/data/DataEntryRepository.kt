package io.holunda.polyflow.view.mongo.data

import io.holunda.camunda.taskpool.api.business.EntryType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Repository for data entry documents.
 */
@Repository
interface DataEntryRepository :
  ReactiveMongoRepository<DataEntryDocument, String>, DataEntryUpdateRepository {

  /**
   * Find by id excluding entries marked as deleted.
   */
  @Query("{ '_id': ?0, 'deleted': { \$ne: true } }")
  fun findNotDeletedById(id: String): Mono<DataEntryDocument>

  /**
   * Find by multiple ids excluding entries marked as deleted.
   */
  @Query("{ '_id': { \$in: ?0 }, 'deleted': { \$ne: true } }")
  fun findNotDeletedById(id: Iterable<String>): Flux<DataEntryDocument>

  /**
   * Find all data entries that are not marked as deleted.
   */
  @Query("{ 'deleted': { \$ne: true } }")
  fun findNotDeleted(sort: Sort? = null): Flux<DataEntryDocument>

  /**
   * Reactive finder by type.
   */
  @Query("{ 'entryType': ?0, 'deleted': { \$ne: true } }")
  fun findAllByEntryType(entryType: EntryType): Flux<DataEntryDocument>

  /**
   * Finder for user.
   */
  @Query("{ \$or: [ { 'authorizedUsers' : ?0 }, { 'authorizedGroups' : { \$in: ?1 } } ], 'deleted': { \$ne: true } }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): Flux<DataEntryDocument>

  /**
   * Finder for user.
   */
  @Query("{ \$or: [ { 'authorizedUsers' : ?0 }, { 'authorizedGroups' : { \$in: ?1 } } ], 'deleted': { \$ne: true }, 'protocol.username': ?0 }")
  fun findAllForUserWithInvolvement(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): Flux<DataEntryDocument>

  /**
   * Retrieves all data entries for user.
   */
  @Query("{ \$or: [ { 'authorizedUsers' : ?0 }, { 'authorizedGroups' : { \$in: ?1 } } ], 'deleted': { \$ne: true } }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>, pageable: Pageable? = null): Flux<DataEntryDocument>

  /**
   * Finds all data deleted entries, deleted before specified time.
   */
  @Query("{ 'deleted': true, 'deleteTime': { \$not: { \$gt: ?0 } } }")
  fun findDeletedBefore(deleteTime: Instant): Flux<DataEntryDocument>
}

