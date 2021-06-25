package io.holunda.polyflow.view.mongo

import org.springframework.context.annotation.Import

/**
 * Enables polyflow projection using Mongo DB as persistence.
 */
@MustBeDocumented
@Import(io.holunda.polyflow.view.mongo.TaskPoolMongoViewConfiguration::class)
annotation class EnableTaskPoolMongoView
