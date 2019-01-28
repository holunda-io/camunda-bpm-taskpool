package io.holunda.camunda.taskpool.cockpit.rest.impl

import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import io.holunda.camunda.taskpool.cockpit.rest.Rest
import io.holunda.camunda.taskpool.cockpit.service.TaskPoolCockpitService
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(path = [Rest.PATH])
open class CockpitTaskOperationController(
  private val gateway: CommandGateway,
  private val cockpitService: TaskPoolCockpitService
) {

  companion object : KLogging()

  @PostMapping(path = ["/task/{taskId}/{command}"])
  fun sendCommand(@PathVariable(name = "taskId") taskId: String, @PathVariable(name = "command") command: String): Mono<Void> {
    when (command) {
      "delete" -> delete(taskId)
      else -> throw UnsupportedOperationException("Unsupported operation $command")
    }

    return Mono.empty()
  }

  open fun delete(taskId: String) {
    val taskReference = cockpitService.findTaskReference(taskId)
    val command = DeleteTaskCommand(
      id = taskReference.id,
      deleteReason = "Deleted from cockpit"
    )
    gateway.sendAndWait<Any>(command)
  }
}
