package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import io.holunda.camunda.taskpool.example.tasklist.TaskUrlResolverProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.view.Task
import org.apache.commons.lang3.text.StrSubstitutor

/**
 * Creates a complete REST URL to call the tasks form.
 */
interface TaskUrlResolver {
  fun resolveUrl(task: Task): String
}

class DefaultTaskUrlResolver(
  private val lookup: ApplicationUrlLookup,
  private val props: TaskUrlResolverProperties,
  private val objectMapper: ObjectMapper = jacksonObjectMapper()
) : TaskUrlResolver {

  override fun resolveUrl(task: Task): String {
    val appUrl = lookup.lookup(task.sourceReference.applicationName)
    val tmpl = props.getUrlTemplate(task.taskDefinitionKey)


    val taskMap: Map<String, Any> = objectMapper.convertValue(task, object : TypeReference<Map<String, Any>>() {})

    return "$appUrl/${StrSubstitutor(taskMap).replace(tmpl)}"
  }

}

