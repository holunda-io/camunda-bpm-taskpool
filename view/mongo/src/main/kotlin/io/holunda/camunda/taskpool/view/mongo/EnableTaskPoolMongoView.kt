package io.holunda.camunda.taskpool.view.mongo

import org.springframework.context.annotation.Import

/**
 * Enables mongo db taskpool view
 */
@MustBeDocumented
@Import(TaskPoolMongoViewConfiguration::class)
annotation class EnableTaskPoolMongoView
