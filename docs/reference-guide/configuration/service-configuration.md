# Service Configuration

## Axon service flow

Polyflow services communicate through Axon Framework. A collector converts an
incoming process-engine trigger into an Axon command and sends it through the
command bus to the core service. The core service loads its event-sourced
aggregates, applies events, and publishes them through the event bus. A view
service consumes that stream with tracking event processors and updates its
projections.

This flow determines each service's runtime configuration. It is separate from
the Liquibase configuration, which only provisions the database objects needed
by the selected service. See [Persistence configuration](persistence.md) for
the master changelog selected by each deployment topology.

## Core service

The core service owns Polyflow aggregates and command handling. It needs an
Axon event store so aggregates can be loaded from their event history:

- With Axon Server, configure the Axon Server connection and use its event
  store.
- Without Axon Server, configure a durable relational Axon event store. The
  core Liquibase master provisions `DOMAIN_EVENT_ENTRY` and
  `SNAPSHOT_EVENT_ENTRY` for this purpose.

The core service also handles events and therefore needs token and dead-letter
storage. The core master includes `TOKEN_ENTRY` and `DEAD_LETTER_ENTRY`.

## View service

An independently deployed view service is an event-tracking consumer. It does
not load event-sourced aggregates and does not own the event store. Its durable
state is its projections, the tracking token that identifies its event-stream
position, and dead letters.

Configure the independent view application with an in-memory Axon event store
and saga store. Expose both as Spring beans so Axon uses them instead of JPA
event-sourcing or saga persistence. The view Liquibase master still provides
the projection, token, and dead-letter objects.

```kotlin
import io.holunda.polyflow.view.jpa.EnablePolyflowJpaView
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ViewAxonConfiguration {

  @Bean
  fun eventStorageEngine() = InMemoryEventStorageEngine()

  @Bean
  fun sagaStore() = InMemorySagaStore()
}

@SpringBootApplication
@EnablePolyflowJpaView
class ViewApplication
```

`@EnablePolyflowJpaView` is required for a JPA view service. It imports the
Polyflow view configuration, including the JPA entity scan for projection
entities and Axon's `TokenEntry` and `DeadLetterEntry` entities. Do not rely on
the application's `@SpringBootApplication` package scan to discover them: the
application package commonly does not contain the Polyflow entity packages.

Do not use this configuration in a monolith where the core and view share the
same Axon configuration: that service needs the core's durable or Axon Server
event store.

## Event processing and sagas

Both core and view services process events. Both therefore require
`TOKEN_ENTRY` and `DEAD_LETTER_ENTRY`; a dead-letter queue is event-processing
infrastructure, not event-sourcing or saga infrastructure.

Persistent sagas are optional in both services. Use Axon's in-memory saga store
unless a service intentionally declares persistent sagas. The Polyflow core and
view masters do not provision saga tables.
