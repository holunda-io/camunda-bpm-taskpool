package io.holunda.polyflow.taskpool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration

/**
 * Configuration for a job handler sending commands to Taskpool Core.
 * @param taskId for what task id we are sending commands
 * @param commandByteArrayId reference to the resource entity in ACT_GE_BYTEARRAY storing byte serialized JSON of List<EngineTaskCommand>.
 */
data class EngineTaskCommandsSendingJobHandlerConfiguration(
  val taskId: String,
  val commandByteArrayId: String
) : JobHandlerConfiguration {

  companion object {
    /**
     * Reconstructs the configuration from JSON String.
     */
    fun fromCanonicalString(value: String, objectMapper: ObjectMapper): EngineTaskCommandsSendingJobHandlerConfiguration {
      return objectMapper.readValue(value)
    }
  }

  // will never be used.
  override fun toCanonicalString(): String = throw UnsupportedOperationException()
}