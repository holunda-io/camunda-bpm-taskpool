package io.holunda.camunda.taskpool.sender.accumulator

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * Accumulator is responsible for transforming (and evtl. reducing) the number of commands being sent,
 * by accumulating information from several commands into one.
 */
typealias CommandAccumulator = (List<EngineTaskCommand>) -> List<EngineTaskCommand>
