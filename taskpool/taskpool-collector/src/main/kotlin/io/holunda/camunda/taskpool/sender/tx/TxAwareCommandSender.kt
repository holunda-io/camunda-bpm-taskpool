package io.holunda.camunda.taskpool.sender.tx

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.enricher.VariablesEnricher
import io.holunda.camunda.taskpool.sender.CommandSender
import io.holunda.camunda.taskpool.sender.simple.SimpleCommandSender
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization

/**
 * Leverages the simple task command sender, by executing it after the TX commit.
 */
class TxAwareCommandSender(
  gateway: CommandGateway,
  properties: TaskCollectorProperties,
  enricher: VariablesEnricher
) : CommandSender {

  private val simpleTaskCommandSender = SimpleCommandSender(gateway = gateway, properties = properties, enricher = enricher)

  override fun send(command: Any) {

    registerSynchronization(object : TransactionSynchronizationAdapter() {
      override fun afterCommit() {
        simpleTaskCommandSender.send(command)
      }
    })
  }

}
