package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.sender.EngineTaskCommandSender
import io.holunda.camunda.taskpool.sender.task.TxAwareAccumulatingEngineTaskCommandSender
import io.holunda.camunda.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SenderConfiguration {

  /**
   * Create command sender.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.task.sender.type}' != 'custom'")
  fun txCommandSender(properties: SenderProperties, commandListGateway: CommandListGateway, accumulator: EngineTaskCommandAccumulator): EngineTaskCommandSender =
    when (properties.type) {
      SenderType.tx -> TxAwareAccumulatingEngineTaskCommandSender(commandListGateway, accumulator, properties.sendWithinTransaction)
      else -> throw IllegalStateException("Could not initialize sender, used unknown  ${properties.type} type.")
    }

}
