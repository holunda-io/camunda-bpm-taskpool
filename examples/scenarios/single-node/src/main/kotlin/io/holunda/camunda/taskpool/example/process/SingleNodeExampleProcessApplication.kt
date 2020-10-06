package io.holunda.camunda.taskpool.example.process

import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.datapool.core.EnableDataPool
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.example.tasklist.EnableTasklist
import io.holunda.camunda.taskpool.example.users.EnableExampleUsers
import io.holunda.camunda.taskpool.urlresolver.EnablePropertyBasedFormUrlResolver
import io.holunda.camunda.taskpool.view.simple.EnableTaskPoolSimpleView
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


fun main(args: Array<String>) {
  SpringApplication.run(SingleNodeExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableExampleUsers
@EnableTaskPoolSimpleView
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
