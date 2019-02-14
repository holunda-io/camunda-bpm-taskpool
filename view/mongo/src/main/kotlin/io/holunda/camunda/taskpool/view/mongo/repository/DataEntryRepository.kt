package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.camunda.taskpool.api.business.dataIdentity
import io.holunda.camunda.taskpool.view.DataEntry
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository for data entry documents.
 */
@Repository
interface DataEntryRepository : MongoRepository<DataEntryDocument, String> {

  fun findByIdentity(identity: DataIdentity) = findById(dataIdentity(entryId = identity.entryId, entryType = identity.entryType))
  fun findAllByEntryType(entryType: EntryType): List<DataEntryDocument>

}
