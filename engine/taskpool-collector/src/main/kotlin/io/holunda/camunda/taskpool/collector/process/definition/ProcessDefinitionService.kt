package io.holunda.camunda.taskpool.process

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.RegisterProcessDefinitionCommand
import io.holunda.camunda.taskpool.candidateGroups
import io.holunda.camunda.taskpool.candidateUsers
import io.holunda.camunda.taskpool.executeInCommandContext
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.springframework.stereotype.Component

/**
 * Component responsible for retrieving process definitions from process engine.
 */
@Component
class ProcessDefinitionService(
  private val properties: TaskCollectorProperties
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
    if (processDefinitionKey != null && processDefinitionKey.isNotBlank()) {
      query.processDefinitionKey(processDefinitionKey)
    }
    val newDefinitions: List<ProcessDefinitionEntity> = query.list()
      .filter { returnAll || !processDefinitions.map { def -> def.id }.contains(it.id) }
      .filter { it is ProcessDefinitionEntity }
      .map { it as ProcessDefinitionEntity }

    if (returnAll) {
      this.processDefinitions.clear()
    }
    this.processDefinitions.addAll(newDefinitions)

    return newDefinitions.map { it.asCommand(applicationName = properties.enricher.applicationName, formKey = formService.getStartFormKey(it.id)) }
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
    return cfg.executeInCommandContext(Command {
      RegisterProcessDefinitionCommandList(
        getProcessDefinitions(
          formService = cfg.formService,
          repositoryService = cfg.repositoryService,
          processDefinitionKey = processDefinitionKey,
          returnAll = returnAll)
      )
    }).commands
  }


  private fun ProcessDefinitionEntity.asCommand(applicationName: String, formKey: String?) =
    RegisterProcessDefinitionCommand(
      processDefinitionId = this.id,
      processDefinitionKey = this.key,
      processDefinitionVersion = this.version,
      processName = this.name ?: this.key,
      processVersionTag = this.versionTag,
      processDescription = this.description,
      startableFromTasklist = this.isStartableInTasklist,
      applicationName = applicationName,
      formKey = formKey,
      candidateStarterUsers = this.candidateUsers(),
      candidateStarterGroups = this.candidateGroups()
    )

  /**
   * Result encapsulated in a type to avoid type erasure.
   */
  private data class RegisterProcessDefinitionCommandList(val commands: List<RegisterProcessDefinitionCommand>)
}

