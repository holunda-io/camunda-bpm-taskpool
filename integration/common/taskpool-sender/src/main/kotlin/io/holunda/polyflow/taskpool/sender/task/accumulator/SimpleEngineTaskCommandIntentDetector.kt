package io.holunda.polyflow.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.ASSIGN
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.ATTRIBUTES_LISTENER_UPDATE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CANDIDATE_USER_ADD
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CANDIDATE_USER_DELETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.COMPLETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.DELETE

/**
 * Simple implementation of the intent detection
 */
class SimpleEngineTaskCommandIntentDetector(
  private val simpleIntentDetectionBehaviour: Boolean
) : EngineTaskCommandIntentDetector {

  private val sortingCommandAccumulator = SortingCommandAccumulator()

  override fun detectIntents(engineTaskCommands: List<EngineTaskCommand>): List<List<EngineTaskCommand>> {
    if (simpleIntentDetectionBehaviour) {
      return listOf(sortingCommandAccumulator.invoke(engineTaskCommands))
    }

    val sorted = sortingCommandAccumulator.invoke(engineTaskCommands).toMutableList()
    val intents = mutableListOf<List<EngineTaskCommand>>()

    while (sorted.isNotEmpty()) {
      val head = sorted.removeFirst()
      val projectable = head.filterProjectable(sorted)
      sorted.removeAll(projectable)
      intents.add(listOf(head) + projectable)
    }
    return intents
  }

  /*
   * Finds projectable commands to current.
   * Assumptions:
   * - every command can be projected with other of the same event type
   * - create command can be projected with all other commands, except COMPLETE and DELETE
   * - enforced enrichment of assign with candidate_user_delete (can't work, but this way the unneeded delete command is dropped)
   * - enforced enrichment of assign with candidate_user_add (can't work, but this way the unneeded add command is dropped)
   * - assign can be projected into complete, and then the corresponding candidate users change commands
   */
  private fun EngineTaskCommand.filterProjectable(engineTaskCommands: List<EngineTaskCommand>): List<EngineTaskCommand> =
    engineTaskCommands
      .filter { this.eventName == it.eventName } + // event type itself
      when (this) {
        // CREATE carries any information of ATTRIBUTES, ASSIGN, CANDIDATE-CHANGE
        is CreateTaskCommand -> engineTaskCommands.filter { it.eventName !in setOf(COMPLETE, DELETE) }
        // candidate user delete is fired if the task gets re-assigned
        // candidate user add is fired if the task gets assigned
        is AssignTaskCommand -> engineTaskCommands.filter { it.eventName in setOf(CANDIDATE_USER_ADD, CANDIDATE_USER_DELETE, ATTRIBUTES_LISTENER_UPDATE) }
        // is assignment is executed, it can be carried with complete and then the candidate user add / delete
        is CompleteTaskCommand -> if (engineTaskCommands.any { it.eventName == ASSIGN }) {
          engineTaskCommands.filter { it.eventName in setOf(ASSIGN, CANDIDATE_USER_ADD, CANDIDATE_USER_DELETE, ATTRIBUTES_LISTENER_UPDATE) }
        } else {
          // complete without assign is not projectable
          // FIXME: think how attribute change on complete can be carried to the core.
          listOf()
        }
        // updates of the listener are accepted
        is UpdateAttributeTaskCommand -> engineTaskCommands.filter { it.eventName == ATTRIBUTES_LISTENER_UPDATE }
        is UpdateAssignmentTaskCommand -> engineTaskCommands.filter { it is UpdateAttributeTaskCommand && it.unchanged}
        else -> listOf()
      }
}
