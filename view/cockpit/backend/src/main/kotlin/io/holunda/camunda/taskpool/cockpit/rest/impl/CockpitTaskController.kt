package io.holunda.camunda.taskpool.cockpit.rest.impl

import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import io.holunda.camunda.taskpool.cockpit.rest.Rest
import io.holunda.camunda.taskpool.cockpit.rest.api.TaskApi
import io.holunda.camunda.taskpool.cockpit.service.TaskPoolCockpitService
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = [Rest.PATH])
open class CockpitTaskController(
  private val gateway: CommandGateway,
  private val cockpitService: TaskPoolCockpitService
) : TaskApi {

  companion object : KLogging()

  override fun sendCommand(@PathVariable(name = "taskId") taskId: String, @PathVariable(name = "command") command: String): ResponseEntity<Void> {
    when (command) {
      "delete" -> delete(taskId)
      else -> throw UnsupportedOperationException("Unsupported operation $command")
    }

    return ResponseEntity.noContent().build()
  }

  open fun delete(taskId: String) {
    val taskReference = cockpitService.findTaskReference(taskId)
    val command = DeleteTaskCommand(
      id = taskReference.id,
      taskDefinitionKey = taskReference.taskDefinitionKey,
      sourceReference = taskReference.sourceReference,
      deleteReason = "Deleted from cockpit"
    )
    gateway.sendAndWait<Any>(command)
  }
}
