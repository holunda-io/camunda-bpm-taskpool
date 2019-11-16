package io.holunda.camunda.taskpool.cockpit.rest.impl

import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import io.holunda.camunda.taskpool.cockpit.rest.Rest
import io.holunda.camunda.taskpool.cockpit.service.TaskPoolCockpitService
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Task operation controller.
 */
@RestController
@RequestMapping(path = [Rest.PATH])
class CockpitTaskOperationController(
  private val gateway: CommandGateway,
  private val cockpitService: TaskPoolCockpitService
) {

  companion object : KLogging()

  /**
   * Send a command for a specific task.
   */
  @PostMapping(path = ["/task/{taskId}/{command}"])
  fun sendCommand(@PathVariable(name = "taskId") taskId: String, @PathVariable(name = "command") command: String): Mono<Void> {
    when (command) {
      "delete" -> delete(taskId)
      else -> throw UnsupportedOperationException("Unsupported operation $command")
    }

    return Mono.empty()
  }

  /**
   * Deletes a task.
   */
  fun delete(taskId: String) {
    val taskReference = cockpitService.findTaskReference(taskId)
    val command = DeleteTaskCommand(
      id = taskReference.id,
      deleteReason = "Deleted from cockpit"
    )
    gateway.sendAndWait<Any>(command)
  }
}
