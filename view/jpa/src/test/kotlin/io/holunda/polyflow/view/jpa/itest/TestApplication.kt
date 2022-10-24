package io.holunda.polyflow.view.jpa.itest

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import org.axonframework.eventhandling.deadletter.jpa.DeadLetterEntry
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(ObjectMapperConfiguration::class)
@ComponentScan(basePackages = ["io.holunda.polyflow.view.jpa"])
@EntityScan(
  basePackageClasses = [TokenEntry::class, SagaEntry::class, DomainEventEntry::class, DeadLetterEntry::class]
)
class TestApplication {

  @Bean
  @Qualifier("eventSerializer")
  fun myEventSerializer(): Serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()

}
