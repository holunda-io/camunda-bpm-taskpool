package io.holunda.polyflow.view.jpa.itest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(value = [ObjectMapperConfiguration::class, MockQueryEmitterConfiguration::class])
@ComponentScan(basePackages = ["io.holunda.polyflow.view.jpa"])
class TestApplicationDataJpa
