package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.memberProperties


typealias CommandAccumulator = (List<WithTaskId>) -> List<WithTaskId>

/**
 * Just passing the commands straight through.
 */
class DirectCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<WithTaskId>) = taskCommands
}

/**
 * invert the order of commands, because Camunda sends them in reversed order.
 */
class InvertingCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<WithTaskId>) = taskCommands.reversed()
}

/**
 * invert the order of commands, because Camunda sends them in reversed order.
 */
class SortingCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<WithTaskId>) = taskCommands.sortedWith(CommandSorter())
}

/**
 * invert the order of commands and project attribute to one command
 */
class ProjectingCommandAccumulator : CommandAccumulator {

  private val inverter: CommandAccumulator = InvertingCommandAccumulator()

  override fun invoke(taskCommands: List<WithTaskId>): List<WithTaskId> {
    val reversed = inverter.invoke(taskCommands)
    var command: CreateTaskCommand? = reversed.find { it is CreateTaskCommand } as CreateTaskCommand?
    return if (command != null) {

      val createCommand =
        CreateTaskCommand(
          id = command.id,
          taskDefinitionKey = command.taskDefinitionKey,
          sourceReference = command.sourceReference,

          name = command.name,
          description = command.description,
          priority = command.priority,
          owner = command.owner,
          eventName = CamundaTaskEvent.CREATE,
          dueDate = command.dueDate,
          createTime = command.createTime,
          candidateUsers = command.candidateUsers,
          candidateGroups = command.candidateGroups,
          assignee = command.assignee,
          payload = command.payload,
          businessKey = command.businessKey,
          formKey = command.formKey,
          correlations = command.correlations,
          followUpDate = command.followUpDate,
          enriched = command.enriched
        )

      flatten(createCommand, reversed.subList(1, reversed.size - 1))

      listOf(createCommand)
    } else {
      reversed
    }
  }
}

fun <T : EngineTaskCommand> flatten(command: T, commands: List<WithTaskId>): T {

  val logger: Logger = LoggerFactory.getLogger("FLATTEN")

  return when (command) {
    is CreateTaskCommand -> {
      // deconstruct to properties
      var (
        id,
        sourceReference,
        taskDefinitionKey,
        formKey,
        name,
        description,
        priority,
        createTime,
        owner,
        eventName,
        candidateUsers,
        candidateGroups,
        assignee,
        dueDate,
        followUpDate,
        businessKey,
        payloadcreateVariables,
        correlations,
        enriched
      ) = command

      commands.forEach { task ->
        if (task is EnrichedEngineTaskCommand) {
          task.javaClass.kotlin.memberProperties.filter { prop ->
            prop.get(task) != prop.get(command)
          }.forEach {
            logger.info("Value is different of ${it.name}, new value is ${it.get(task)} != ${it.get(command)}")
          }
        }
      }

      command
    }
    else -> command
  }
}
