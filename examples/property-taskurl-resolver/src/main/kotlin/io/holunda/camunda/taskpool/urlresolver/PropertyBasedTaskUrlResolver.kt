package io.holunda.camunda.taskpool.urlresolver

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskUrlResolver
import org.apache.commons.text.StringSubstitutor

class PropertyBasedTaskUrlResolver(
  private val props: TaskUrlResolverProperties,
  private val objectMapper: ObjectMapper = jacksonObjectMapper()
) : TaskUrlResolver {

  companion object {
    const val APPLICATION_NAME_ATTRIBUTE = "applicationName"
  }

  override fun resolveUrl(task: Task): String {

    val taskMap: Map<String, Any> = objectMapper.convertValue(task, object : TypeReference<Map<String, Any>>() {})
    val applicationName = task.sourceReference.applicationName
    val appMap = mapOf(APPLICATION_NAME_ATTRIBUTE to applicationName)

    val appTemplate = props.getApplicationTemplate(applicationName)
    val taskTemplate = props.getUrlTemplate(applicationName, task.taskDefinitionKey)

    return "${StringSubstitutor(appMap).replace(appTemplate)}/${StringSubstitutor(taskMap).replace(taskTemplate)}"
  }

}

