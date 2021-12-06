package io.holunda.camunda.taskpool.upcast

import io.holunda.camunda.taskpool.upcast.definition.ProcessDefinitionEventJSONNullTo1Upcaster
import io.holunda.camunda.taskpool.upcast.definition.ProcessDefinitionEventXMLNullTo1Upcaster
import io.holunda.camunda.taskpool.upcast.task.*
import org.axonframework.serialization.upcasting.event.EventUpcaster
import org.axonframework.serialization.upcasting.event.EventUpcasterChain

/**
 * Returns a list of [EventUpcaster]s that are defined in Polyflow for task pool events. The list will be augmented as events evolve and new upcasters are
 * added. It is recommended to use this list or the [taskpoolEventUpcasterChain] instead of manually adding all upcasters to your own list / chain. This way,
 * you won't miss new upcasters as they are created.
 *
 * There is a similar list for data pool events: [io.holunda.polyflow.datapool.core.business.upcaster.datapoolEventUpcasters].
 */
fun taskpoolEventUpcasters(): List<EventUpcaster> = listOf(
  ProcessDefinitionEventJSONNullTo1Upcaster(),
  ProcessDefinitionEventXMLNullTo1Upcaster(),
  TaskCreatedEngineEvent3To4Upcaster(),
  TaskCompletedEngineEvent3To4Upcaster(),
  TaskDeletedEngineEvent3To4Upcaster(),
  TaskAssignedEngineEvent3To4Upcaster(),
  TaskAttributeUpdatedEngineEvent3To4Upcaster(),
  TaskCandidateGroupChanged1To4Upcaster(),
  TaskCandidateUserChanged1To4Upcaster(),
  TaskClaimedEvent2To4Upcaster(),
  TaskUnclaimedEvent2To4Upcaster(),
  TaskToBeCompletedEvent2To4Upcaster(),
  TaskDeferredEvent2To4Upcaster(),
  TaskUndeferredEvent2To4Upcaster(),
  TaskAttributeUpdatedEngineEvent4To5Upcaster(),
)

/**
 * Returns a chain of [EventUpcaster]s that are defined in Polyflow for task pool events. The chain will be augmented as events evolve and new upcasters are
 * added. It is recommended to use this chain or the [taskpoolEventUpcasters] list instead of manually adding all upcasters to your own list / chain. This way,
 * you won't miss new upcasters as they are created.
 *
 * There is a similar chain for data pool events: [io.holunda.polyflow.datapool.core.business.upcaster.datapoolEventUpcasterChain].
 */
fun taskpoolEventUpcasterChain(): EventUpcasterChain = EventUpcasterChain(taskpoolEventUpcasters())
