package io.holunda.polyflow.view.simple

import org.springframework.context.annotation.Import

/**
 * Enables simple taskpool view
 */
@MustBeDocumented
@Import(io.holunda.polyflow.view.simple.TaskPoolSimpleViewConfiguration::class)
annotation class EnableTaskPoolSimpleView
