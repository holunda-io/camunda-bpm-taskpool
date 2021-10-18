package io.holunda.polyflow.example.process.approval

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.polyflow.datapool.core.EnablePolyflowDataPool
import io.holunda.polyflow.example.tasklist.EnableTasklist
import io.holunda.polyflow.example.users.EnableExampleUsers
import io.holunda.polyflow.taskpool.core.EnablePolyflowTaskPool
import io.holunda.polyflow.urlresolver.EnablePropertyBasedFormUrlResolver
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

/**
 * Starts the single node application.
 */
@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) {
  runApplication<SingleNodeExampleProcessApplication>().let { Unit }
}

/**
 * Application being everything in one node:
 * - process application
 * - task pool core
 * - data pool core
 * - in-memory view
 * - task list
 */
@SpringBootApplication
@EnableExampleUsers
@EnablePolyflowTaskPool
@EnablePolyflowDataPool
@EnableTasklist
@EnablePropertyBasedFormUrlResolver
class SingleNodeExampleProcessApplication {

  /**
   * Factory function creating correlation data provider for revision information.
   * We don't want to explicitly pump revision meta data from command to event.
   */
  @Bean
  fun revisionAwareCorrelationDataProvider(): CorrelationDataProvider {
    return MultiCorrelationDataProvider<CommandMessage<Any>>(
      listOf(
        MessageOriginProvider(),
        SimpleCorrelationDataProvider(RevisionValue.REVISION_KEY)
      )
    )
  }

  @Bean
  @Qualifier("eventSerializer")
  fun mySerializer(): Serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()

}

