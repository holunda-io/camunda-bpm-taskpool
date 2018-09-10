package io.holunda.camunda.taskpool.view.simple

import org.springframework.context.annotation.Import

/**
 * Enables simple taskpool view
 */
@MustBeDocumented
@Import(TaskPoolSimpleViewConfiguration::class)
annotation class EnableTaskPoolSimpleView {

}
