package io.holunda.polyflow.datapool.core.business.upcaster

import org.axonframework.serialization.upcasting.event.EventUpcaster
import org.axonframework.serialization.upcasting.event.EventUpcasterChain

/**
 * Returns a list of [EventUpcaster]s that are defined in Polyflow for data pool events. The list will be augmented as events evolve and new upcasters are
 * added. It is recommended to use this list or the [datapoolEventUpcasterChain] instead of manually adding all upcasters to your own list / chain. This way,
 * you won't miss new upcasters as they are created.
 *
 * There is a similar list for task pool events: [io.holunda.camunda.taskpool.upcast.taskpoolEventUpcasters].
 */
fun datapoolEventUpcasters(): List<EventUpcaster> = listOf(
  DataEntryCreatedEventUpcaster()
)

/**
 * Returns a chain of [EventUpcaster]s that are defined in Polyflow for data pool events. The chain will be augmented as events evolve and new upcasters are
 * added. It is recommended to use this chain or the [datapoolEventUpcasters] list instead of manually adding all upcasters to your own list / chain. This way,
 * you won't miss new upcasters as they are created.
 *
 * There is a similar chain for task pool events: [io.holunda.camunda.taskpool.upcast.TaskpoolEventUpcastersKt.taskpoolEventUpcasterChain].
 */
fun datapoolEventUpcasterChain(): EventUpcasterChain = EventUpcasterChain(datapoolEventUpcasters())
