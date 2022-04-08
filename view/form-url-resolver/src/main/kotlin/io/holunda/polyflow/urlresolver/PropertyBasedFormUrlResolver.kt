package io.holunda.polyflow.urlresolver

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.config.FallbackPayloadObjectMapperAutoConfiguration.Companion.PAYLOAD_OBJECT_MAPPER
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.FormUrlResolver
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.Task
import org.apache.commons.text.StringSubstitutor
import org.springframework.beans.factory.annotation.Qualifier

/**
 * URL resolver backed with properties from yaml.
 */
class PropertyBasedFormUrlResolver(
  private val props: FormUrlResolverProperties,
  private val objectMapper: ObjectMapper
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

  override fun resolveUrl(dataEntry: DataEntry): String {
    val applicationName = dataEntry.applicationName
    val appMap = mapOf(APPLICATION_NAME_ATTRIBUTE to applicationName)
    val appTemplate = props.getApplicationTemplate(applicationName)

    val boTemplate = props.getDataEntryTemplate(applicationName, dataEntry.entryType)
    val boMap: Map<String, Any> = objectMapper.convertValue(dataEntry, object : TypeReference<Map<String, Any>>() {})

    return "${StringSubstitutor(appMap).replace(appTemplate)}/${StringSubstitutor(boMap).replace(boTemplate)}"
  }

}

