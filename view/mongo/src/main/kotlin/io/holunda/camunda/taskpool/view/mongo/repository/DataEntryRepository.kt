package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.camunda.taskpool.api.business.dataIdentity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository for data entry documents.
 */
@Repository
interface DataEntryRepository : MongoRepository<DataEntryDocument, String> {

  fun findByIdentity(identity: DataIdentity): Optional<DataEntryDocument> = findById(dataIdentity(entryId = identity.entryId, entryType = identity.entryType))
  fun findAllByEntryType(entryType: EntryType): List<DataEntryDocument>

  @Query("{ \$or: [ { 'authorizedUsers' : ?0 }, { 'authorizedGroups' : ?1} ] }")
  fun findAllForUser(@Param("username") username: String, @Param("groupNames") groupNames: Set<String>): List<DataEntryDocument>

}
