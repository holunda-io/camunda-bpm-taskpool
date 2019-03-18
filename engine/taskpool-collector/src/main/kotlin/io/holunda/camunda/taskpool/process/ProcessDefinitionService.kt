package io.holunda.camunda.taskpool.process

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.RegisterProcessDefinitionCommand
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.springframework.stereotype.Component

@Component
class ProcessDefinitionService(
  private val properties: TaskCollectorProperties
) {

  private val processDefinitions: MutableList<ProcessDefinition> = mutableListOf()

  fun getProcessDefinitions(formService: FormService, repositoryService: RepositoryService, processDefinitionKey: String = "", returnAll: Boolean = true): List<RegisterProcessDefinitionCommand> {

    val query = repositoryService.createProcessDefinitionQuery()
    if (processDefinitionKey.isNotBlank()) {
      query.processDefinitionKey(processDefinitionKey)
    }
    val newDefinitions: List<ProcessDefinitionEntity> = query.list()
      .filter { returnAll || !processDefinitions.map { def -> def.id }.contains(it.id) }
      .filter { it is ProcessDefinitionEntity }
      .map { it as ProcessDefinitionEntity }
    this.processDefinitions.addAll(newDefinitions)
    return newDefinitions.map { createCommand(it, formService.getStartFormKey(it.id)) }
  }


  private fun createCommand(processDefinition: ProcessDefinitionEntity, formKey: String?) =
    RegisterProcessDefinitionCommand(
      processDefinitionId = processDefinition.id,
      processDefinitionKey = processDefinition.key,
      processDefinitionVersion = processDefinition.version,
      processName = processDefinition.name,
      processVersionTag = processDefinition.versionTag,
      processDescription = processDefinition.description,
      applicationName = properties.enricher.applicationName,
      startableFromTasklist = processDefinition.isStartableInTasklist,
      formKey = formKey,
      candidateStarterUsers = processDefinition.identityLinks.filter { it.isUser && it.type == "candidate" }.map { it.userId }.toSet(),
      candidateStarterGroups = processDefinition.identityLinks.filter { it.isGroup && it.type == "candidate" }.map { it.groupId }.toSet()
    )
}
