package io.holuda.taskpool.zeebe.worker.task

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.variable.serializer.deserialize
import io.zeebe.client.ZeebeClient
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import io.zeebe.spring.client.annotation.ZeebeWorker
import org.axonframework.commandhandling.CommandResultMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class ZeebeUserTaskWorker(
  private val objectMapper: ObjectMapper,
  private val zeebeClient: ZeebeClient,
  private val commandGateway: CommandGateway,
  private val taskCommandSuccessHandler: TaskCommandSuccessHandler,
  private val taskCommandErrorHandler: TaskCommandErrorHandler
) {


  @ZeebeWorker(type = "user")
  fun zeebeFetchingWorker(client: JobClient, job: ActivatedJob) {

    val createTaskCommand = CreateTaskCommand(
      id = job.elementInstanceKey.toString(),
      taskDefinitionKey = job.elementId,
      formKey = job.customHeaders.getOrElse("formKey") { "no_form_key" } as String,
      payload = deserialize(payload = job.variables, mapper = objectMapper),
      name = job.customHeaders.getOrElse("name") { "unnamed" } as String,
      description = job.customHeaders.getOrElse("description") { null },
      assignee = job.customHeaders.getOrElse("assignee") { null },
      candidateUsers = splitFromField(job.customHeaders, "candidateUser"),
      candidateGroups = splitFromField(job.customHeaders, "candidateGroup"),
      sourceReference = ProcessReference(
        instanceId = job.workflowInstanceKey.toString(),
        executionId = job.key.toString(),
        definitionId = job.workflowKey.toString(),
        definitionKey = job.bpmnProcessId,
        name = job.bpmnProcessId,
        applicationName = zeebeClient.configuration.brokerContactPoint
      )
    )

    commandGateway.send<Any, Any?>(createTaskCommand) { commandMessage, commandResultMessage ->
      if (commandResultMessage.isExceptional) {
        taskCommandErrorHandler.apply(commandMessage, commandResultMessage)
      } else {
        taskCommandSuccessHandler.apply(commandMessage, commandResultMessage)
      }
    }


    // do whatever you need to do
    client.newCompleteCommand(job.key)
      .variables("{\"fooResult\": 1}")
      .send()
      .join()
  }

  private fun splitFromField(headers: Map<String, String>, fieldName: String): Set<String> {
    val asString = headers[fieldName] ?: return emptySet()
    return asString.split(",").map { it.trim() }.toSet()
  }
}

/**
 * Handler for command errors.
 */
interface TaskCommandErrorHandler : BiFunction<Any, CommandResultMessage<out Any?>, Unit>

/**
 * Handler for command results.
 */
interface TaskCommandSuccessHandler : BiFunction<Any, CommandResultMessage<out Any?>, Unit>


/**
 * Error handler, logging the error.
 */
open class LoggingTaskCommandErrorHandler(private val logger: Logger) : TaskCommandErrorHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    logger.error("SENDER-006: Sending command $commandMessage resulted in error", commandResultMessage.exceptionResult())
  }
}

/**
 * Logs success.
 */
open class LoggingTaskCommandSuccessHandler(private val logger: Logger) : TaskCommandSuccessHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    if (logger.isDebugEnabled) {
      logger.debug("SENDER-004: Successfully submitted command $commandMessage")
    }
  }
}
