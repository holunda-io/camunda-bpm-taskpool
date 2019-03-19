package io.holunda.camunda.taskpool.urlresolver

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.view.FormUrlResolver
import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.Task
import org.apache.commons.text.StringSubstitutor

class PropertyBasedFormUrlResolver(
  private val props: FormUrlResolverProperties,
  private val objectMapper: ObjectMapper = jacksonObjectMapper()
) : FormUrlResolver {

  companion object {
    const val APPLICATION_NAME_ATTRIBUTE = "applicationName"
  }

  override fun resolveUrl(task: Task): String {

    val applicationName = task.sourceReference.applicationName
    val appMap = mapOf(APPLICATION_NAME_ATTRIBUTE to applicationName)
    val appTemplate = props.getApplicationTemplate(applicationName)

    val taskTemplate = props.getTaskUrlTemplate(applicationName, task.taskDefinitionKey)
    val taskMap: Map<String, Any> = objectMapper.convertValue(task, object : TypeReference<Map<String, Any>>() {})

    return "${StringSubstitutor(appMap).replace(appTemplate)}/${StringSubstitutor(taskMap).replace(taskTemplate)}"
  }

  override fun resolveUrl(processDefinition: ProcessDefinition): String {

    val applicationName = processDefinition.applicationName
    val appMap = mapOf(APPLICATION_NAME_ATTRIBUTE to applicationName)
    val appTemplate = props.getApplicationTemplate(applicationName)

    val processDefinitionTemplate = props.getProcessUrlTemplate(applicationName, processDefinition.processDefinitionKey)
    val processDefinitionMap: Map<String, Any> = objectMapper.convertValue(processDefinition, object : TypeReference<Map<String, Any>>() {})

    return "${StringSubstitutor(appMap).replace(appTemplate)}/${StringSubstitutor(processDefinitionMap).replace(processDefinitionTemplate)}"

  }
}

