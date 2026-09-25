package io.holunda.polyflow.datapool.core.itest

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import io.holunda.polyflow.datapool.core.EnablePolyflowDataPool
import org.axonframework.eventhandling.deadletter.jpa.DeadLetterEntry
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnablePolyflowDataPool
@EntityScan(
  basePackageClasses = [TestApplication::class] // disable axon default scan
)
class TestApplication {
  @Bean
  @Qualifier("eventSerializer")
  fun myEventSerializerForProcess(): Serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()

  @Bean
  fun tokenStore() = InMemoryTokenStore()

  @Bean
  fun inMemoryStorageEngine() = InMemoryEventStorageEngine()

  @Bean
  fun sagaStore() = InMemorySagaStore()
}
