package io.holunda.camunda.taskpool.example.process.view.simple

import io.holunda.polyflow.view.simple.EnableTaskPoolSimpleView
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!mongo")
@io.holunda.polyflow.view.simple.EnableTaskPoolSimpleView
class SimpleViewConfiguration
