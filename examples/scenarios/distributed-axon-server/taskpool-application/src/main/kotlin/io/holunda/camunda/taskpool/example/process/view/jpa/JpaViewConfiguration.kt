package io.holunda.camunda.taskpool.example.process.view.jpa

import io.holunda.polyflow.view.jpa.EnablePolyflowJpaView
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("jpa")
@EnablePolyflowJpaView
class JpaViewConfiguration
