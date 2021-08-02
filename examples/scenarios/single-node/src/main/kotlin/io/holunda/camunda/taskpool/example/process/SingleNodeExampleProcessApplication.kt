package io.holunda.camunda.taskpool.example.process

import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.datapool.core.EnableDataPool
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.example.tasklist.EnableTasklist
import io.holunda.camunda.taskpool.example.users.EnableExampleUsers
import io.holunda.polyflow.urlresolver.EnablePropertyBasedFormUrlResolver
import io.holunda.polyflow.view.simple.EnablePolyflowSimpleView
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

/**
 * Starts the single node application.
 */
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
@EnablePolyflowSimpleView
@EnableTaskPool
@EnableDataPool
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
}
