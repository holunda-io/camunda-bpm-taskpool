package io.holunda.polyflow.view.mongo

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import com.tngtech.jgiven.integration.spring.EnableJGiven
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableJGiven
class TaskPoolMongoTestContext {
  @Bean
  fun queryUpdateEmitter(): QueryUpdateEmitter = Mockito.mock(QueryUpdateEmitter::class.java)

  @Bean
  fun eventProcessingConfiguration(): EventProcessingConfiguration = Mockito.mock(EventProcessingConfiguration::class.java)

  @Bean
  @Qualifier("eventSerializer")
  fun myEventSerializer(): Serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()
}
