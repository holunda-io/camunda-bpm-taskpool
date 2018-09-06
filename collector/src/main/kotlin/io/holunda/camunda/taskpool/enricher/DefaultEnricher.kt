package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
open class DefaultEnricher {

  @Bean
  @ConditionalOnMissingBean(CreateCommandEnricher::class)
  open fun defaultCreateEnricher(): CreateCommandEnricher = EmptyCreateCommandEnricher()

  @Bean
  @ConditionalOnMissingBean(AssignCommandEnricher::class)
  open fun defaultAssignEnricher(): AssignCommandEnricher = EmptyAssignCommandEnricher()

  @Bean
  @ConditionalOnMissingBean(DeleteCommandEnricher::class)
  open fun defaultDeleteEnricher(): DeleteCommandEnricher = EmptyDeleteCommandEnricher()

  @Bean
  @ConditionalOnMissingBean(CompleteCommandEnricher::class)
  open fun defaultCompleteEnricher(): CompleteCommandEnricher = EmptyCompleteCommandEnricher()
}

class EmptyCreateCommandEnricher : CreateCommandEnricher {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CreateTaskCommand): CreateTaskCommand = command.apply { enriched = true }
}

class EmptyCompleteCommandEnricher : CompleteCommandEnricher {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CompleteTaskCommand): CompleteTaskCommand = command.apply { enriched = true }
}

class EmptyDeleteCommandEnricher : DeleteCommandEnricher {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: DeleteTaskCommand): DeleteTaskCommand = command.apply { enriched = true }
}

class EmptyAssignCommandEnricher : AssignCommandEnricher {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: AssignTaskCommand): AssignTaskCommand = command.apply { enriched = true }
}

