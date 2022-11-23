package io.holunda.polyflow.datapool.core.itest

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import io.holunda.polyflow.datapool.core.EnablePolyflowDataPool
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnablePolyflowDataPool
class TestApplication {
  @Bean
  @Qualifier("eventSerializer")
  fun myEventSerializerForProcess(): Serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()

}
