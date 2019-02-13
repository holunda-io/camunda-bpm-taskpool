package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.dataIdentity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Repository for data entry documents.
 */
@Repository
interface DataEntryRepository : MongoRepository<DataEntryDocument, String> {

  fun findByIdentity(identity: DataIdentity) = findById(dataIdentity(entryId = identity.entryId, entryType = identity.entryType))
}
