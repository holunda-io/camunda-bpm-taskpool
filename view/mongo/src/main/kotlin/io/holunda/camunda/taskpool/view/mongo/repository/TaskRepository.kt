package io.holunda.camunda.taskpool.view.mongo.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


/**
 * Repository for task documents.
 */
@Repository
interface TaskRepository : MongoRepository<TaskDocument, String>

