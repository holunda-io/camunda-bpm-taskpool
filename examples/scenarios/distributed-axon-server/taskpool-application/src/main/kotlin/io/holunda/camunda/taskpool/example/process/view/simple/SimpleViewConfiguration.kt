package io.holunda.camunda.taskpool.example.process.view.simple

import io.holunda.polyflow.view.mongo.EnablePolyflowMongoView
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!mongo")
@EnablePolyflowMongoView
class SimpleViewConfiguration
