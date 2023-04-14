package io.holunda.polyflow.taskpool.collector.task

import io.holunda.camunda.taskpool.api.task.EngineTaskCommandFilter
import io.holunda.polyflow.taskpool.asCreateCommand
import io.holunda.polyflow.taskpool.callInProcessEngineContext
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.view.query.PageableSortableQuery
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.springframework.context.ApplicationEventPublisher

/**
 * Service to collect tasks and fire the corresponding commands using Camunda Task Service.
 */
class TaskServiceCollectorService(
  private val taskService: TaskService,
  private val commandExecutor: CommandExecutor,
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties,
  private val applicationEventPublisher: ApplicationEventPublisher,
  private val engineTaskCommandFilter: EngineTaskCommandFilter
) {

  /**
   * Collects tasks existing inm the Camunda task service
   * sends commands for all which are not filtered away by the filter.
   * In combination with [TaskAggregateEngineTaskCommandFilter] this can be used to initialize
   * the event store with tasks from the engine.
   * @param activeOnly parameter controlling if only active tasks are collected.
   * @param firstResult first result of filtered command list to be sent
   * @param maxResults last result of filtered command list to be sent
   */
  fun collectAndPopulateExistingTasks(activeOnly: Boolean = true, firstResult: Int = 0, maxResults: Int = 1000) {
    callInProcessEngineContext(newContext = true) {
      commandExecutor.execute { innerContext ->
        innerContext.registerCommandContextListener(object : CommandContextListener {
          override fun onCommandContextClose(commandContext: CommandContext) {
            // Remove all cached entities from the inner context because we don't want to accidentally flush any changes that could cause
            // OptimisticLockingExceptions in the outer context.
            // (We don't change any variables but there are situations where _reading_ a variable also causes a change, e.g. when the serialized form of a
            // complex variable has changed because properties have been added since creation of the variable. Another example: A variable containing a Set
            // has been created and serialized from a LinkedHashSet with a specific order, but is deserialized to a HashSet with a different iteration order.
            // When this is serialized again, the serialized form will look different because the order has changed.
            (commandContext.sessions[DbEntityManager::class.java] as? DbEntityManager)?.dbEntityCache?.apply { cachedEntities.forEach { remove(it) } }
          }

          override fun onCommandFailed(commandContext: CommandContext, t: Throwable) {
          }
        })

        // query
        val engineTasks = taskService
          .createTaskQuery()
          .initializeFormKeys()
          .let {
            if (activeOnly) {
              it.active()
            } else {
              it
            }
          }.list()

        // create commands
        val commands = engineTasks.filterIsInstance<TaskEntity>().map { task -> task.asCreateCommand(camundaTaskpoolCollectorProperties.applicationName) }

        // filter and limit
        val filtered = commands.filter { command -> engineTaskCommandFilter.test(command) }.let {
          if (firstResult < it.size) {
            it.subList(firstResult, maxResults.coerceAtMost(it.size))
          } else {
            it
          }
        }

        // publish
        filtered.forEach {
          applicationEventPublisher.publishEvent(it)
        }
      }
    }
  }
}
