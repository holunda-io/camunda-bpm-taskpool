package io.holunda.polyflow.liquibase

import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class LiquibaseTestApplication {

  @Bean
  fun inMemoryEventStorageEngine() = InMemoryEventStorageEngine()

  @Bean
  fun inMemorySagaStore() = InMemorySagaStore()
}
