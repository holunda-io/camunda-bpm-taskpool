package io.holunda.polyflow.example.tasklist

import org.springframework.context.annotation.Import


/**
 * Enables simple taskpool view
 */
@MustBeDocumented
@Import(TasklistConfiguration::class)
annotation class EnableTasklist
