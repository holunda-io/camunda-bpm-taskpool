package io.holunda.polyflow.view.jpa.itest

import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(ObjectMapperConfiguration::class)
@ComponentScan(basePackages = ["io.holunda.polyflow.view.jpa"])
@EntityScan(
  basePackageClasses = [TokenEntry::class, SagaEntry::class, DomainEventEntry::class]
)
class TestApplication
