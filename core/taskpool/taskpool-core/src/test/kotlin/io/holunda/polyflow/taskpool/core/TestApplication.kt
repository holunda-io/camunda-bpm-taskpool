package io.holunda.polyflow.taskpool.core

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnablePolyflowTaskPool
class TestApplication {
  @Bean
  @Qualifier("eventSerializer")
  fun myEventSerializerForProcess(): Serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()

}
