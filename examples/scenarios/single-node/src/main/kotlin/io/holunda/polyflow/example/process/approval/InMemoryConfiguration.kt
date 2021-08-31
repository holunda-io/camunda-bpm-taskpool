package io.holunda.polyflow.example.process.approval

import io.holunda.polyflow.view.simple.EnablePolyflowSimpleView
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mem")
@EnablePolyflowSimpleView
class InMemoryConfiguration
