package io.holunda.polyflow.view.mongo

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import com.tngtech.jgiven.integration.spring.EnableJGiven
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import java.time.Clock


@Configuration
@EnableJGiven
class TaskPoolMongoTestContext {
  @Bean
  fun queryUpdateEmitter(): QueryUpdateEmitter = mock()

  @Bean
  fun eventProcessingConfiguration(): EventProcessingConfiguration = mock()

  @Bean
  @Qualifier("eventSerializer")
  fun myEventSerializer(): Serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()

  @Bean
  fun taskScheduler() = mock<TaskScheduler>().apply {
    whenever(clock).thenReturn(Clock.systemUTC())
  }
}
