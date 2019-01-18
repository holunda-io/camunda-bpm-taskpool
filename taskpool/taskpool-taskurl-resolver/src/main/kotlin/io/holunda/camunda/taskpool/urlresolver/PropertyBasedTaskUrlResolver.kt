package io.holunda.camunda.taskpool.urlresolver

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskUrlResolver
import org.apache.commons.text.StringSubstitutor

class PropertyBasedTaskUrlResolver(
  private val lookup: ApplicationUrlLookup,
  private val props: TaskUrlResolverProperties,
  private val objectMapper: ObjectMapper = jacksonObjectMapper()
) : TaskUrlResolver {

  override fun resolveUrl(task: Task): String {
    val appUrl = lookup.lookup(task.sourceReference.applicationName)
    val template = props.getUrlTemplate(task.taskDefinitionKey)
    val taskMap: Map<String, Any> = objectMapper.convertValue(task, object : TypeReference<Map<String, Any>>() {})
    return "$appUrl/${StringSubstitutor(taskMap).replace(template)}"
  }

}

