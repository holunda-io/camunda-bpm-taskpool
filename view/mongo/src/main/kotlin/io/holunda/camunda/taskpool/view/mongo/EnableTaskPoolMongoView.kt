package io.holunda.camunda.taskpool.view.mongo

import org.springframework.context.annotation.Import

/**
 * Enables polyflow projection using Mongo DB as persistence.
 */
@MustBeDocumented
@Import(TaskPoolMongoViewConfiguration::class)
annotation class EnableTaskPoolMongoView
