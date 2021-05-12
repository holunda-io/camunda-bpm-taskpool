package io.holunda.camunda.taskpool.view.mongo.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository


/**
 * Reactive mongo repository for tasks with data entries.
 */
@Repository
interface TaskWithDataEntriesRepository : TaskWithDataEntriesRepositoryExtension, ReactiveMongoRepository<TaskWithDataEntriesDocument, String>

