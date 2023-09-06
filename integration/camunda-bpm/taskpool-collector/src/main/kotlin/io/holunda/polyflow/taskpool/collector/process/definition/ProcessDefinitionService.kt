package io.holunda.polyflow.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import io.holunda.polyflow.taskpool.asCommand
import io.holunda.polyflow.taskpool.executeInCommandContext
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.springframework.stereotype.Component

/**
 * Component responsible for retrieving process definitions from process engine.
 */
@Component
class ProcessDefinitionService(
  private val collectorProperties: CamundaTaskpoolCollectorProperties
) {

  private val processDefinitions: MutableSet<ProcessDefinition> = mutableSetOf()


  /**
   * Retrieves the list of process definition commands, carrying information about start forms and auth information
   * about potential starters.
   *
   * This method must be called in a Camunda command context (eg. from Job or Command).
   * @see {ProcessDefinitionService.getProcessDefinitions(ProcessEngineConfigurationImpl, String, Boolean)}
   */
  fun getProcessDefinitions(
    formService: FormService,
    repositoryService: RepositoryService,
    processDefinitionKey: String? = null,
    returnAll: Boolean = true
  ): List<RegisterProcessDefinitionCommand> {

    require(Context.getCommandContext() != null) { "This method must be executed inside a Camunda command context." }

    val query = repositoryService.createProcessDefinitionQuery()
    if (!processDefinitionKey.isNullOrBlank()) {
      query.processDefinitionKey(processDefinitionKey)
    }
    val newDefinitions: List<ProcessDefinitionEntity> = query.list()
      .filter { returnAll || !processDefinitions.map { def -> def.id }.contains(it.id) }
      .filterIsInstance<ProcessDefinitionEntity>()
    if (returnAll) {
      this.processDefinitions.clear()
    }
    this.processDefinitions.addAll(newDefinitions)
    return newDefinitions.map { it.asCommand(applicationName = collectorProperties.applicationName, formKey = getStartFormKey(it, formService)) }
  }

  private fun getStartFormKey(processDefinitionEntity: ProcessDefinitionEntity, formService: FormService): String? {
    return if (processDefinitionEntity.hasStartFormKey) {
      formService.getStartFormKey(processDefinitionEntity.id)
    } else {
      null
    }
  }

  /**
   * Retrieves the list of process definition commands, carrying information about start forms and auth information
   * about potential starters.
   *
   * Runs the query in a new command context, created by this method.
   */
  fun getProcessDefinitions(
    cfg: ProcessEngineConfigurationImpl,
    processDefinitionKey: String? = null,
    returnAll: Boolean = true
  ): List<RegisterProcessDefinitionCommand> {
    return cfg.executeInCommandContext {
      RegisterProcessDefinitionCommandList(
        getProcessDefinitions(
          formService = cfg.formService,
          repositoryService = cfg.repositoryService,
          processDefinitionKey = processDefinitionKey,
          returnAll = returnAll
        )
      )
    }.commands
  }


  /**
   * Result encapsulated in a type to avoid type erasure.
   */
  private data class RegisterProcessDefinitionCommandList(val commands: List<RegisterProcessDefinitionCommand>)
}

