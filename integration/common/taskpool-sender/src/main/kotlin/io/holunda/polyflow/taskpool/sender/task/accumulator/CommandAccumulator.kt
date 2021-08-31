package io.holunda.polyflow.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * Accumulator is responsible for transforming (and evtl. reducing) the number of commands being sent,
 * by accumulating information from several commands into one.
 */
typealias EngineTaskCommandAccumulator = (List<EngineTaskCommand>) -> List<EngineTaskCommand>
