package io.holunda.camunda.taskpool.core.process

import mu.KLogging
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.spring.stereotype.Aggregate

/**
 * Main representation of the tasks available in the system.
 */
@Aggregate
class ProcessVariableAggregate() {

  companion object : KLogging()

  @AggregateIdentifier
  private lateinit var id: String

}
