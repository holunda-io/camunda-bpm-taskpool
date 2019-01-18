package io.holunda.camunda.taskpool.urlresolver

import org.springframework.context.annotation.Import

@MustBeDocumented
@Import(PropertyBasedTaskUrlResolverConfiguration::class)
annotation class EnablePropertyBasedTaskUrlResolver
