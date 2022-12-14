package io.holunda.polyflow.taskpool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.taskpool.sender.task.TxAwareAccumulatingEngineTaskCommandSender
import io.holunda.polyflow.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity
import java.time.Instant
import java.util.*

/**
 * Command sender writing a Camunda Jobj which will send commands later.
 */
class TxAwareAccumulatingCamundaJobEngineTaskCommandSender(
  private val processEngineConfiguration: ProcessEngineConfigurationImpl,
  private val objectMapper: ObjectMapper,
  engineTaskCommandAccumulator: EngineTaskCommandAccumulator,
  senderProperties: SenderProperties
) : TxAwareAccumulatingEngineTaskCommandSender(
  engineTaskCommandAccumulator = engineTaskCommandAccumulator,
  senderProperties = senderProperties,
) {

  override fun send() {

    // iterate over messages and send them
    taskCommands.get().forEach { (taskId, taskCommands) ->
      // handle messages for every task
      val accumulatorName = engineTaskCommandAccumulator::class.simpleName
      logger.debug("SENDER-005: Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName")
      val commands = engineTaskCommandAccumulator.invoke(taskCommands)


      if (senderProperties.enabled && senderProperties.task.enabled) {
        logger.trace {
          "SENDER-TRACE: Sending commands for task [${taskId}]: " + commands.joinToString(", ", "'", "'", -1, "...") { it.eventName }
        }
        processEngineConfiguration.commandExecutorTxRequired.execute { context ->
          // generate id
          val id = processEngineConfiguration.idGenerator.nextId
          // create resource entity
          val resourceEntity = ResourceEntity().apply {
            this.id = id
            this.bytes = objectMapper.writeValueAsBytes(commands)
          }
          // insert resource entity
          context.resourceManager.insertResource(resourceEntity)

          // create job
          val job = MessageEntity().apply {
            jobHandlerConfigurationRaw = objectMapper.writeValueAsString(
              EngineTaskCommandsSendingJobHandlerConfiguration(taskId = taskId, commandByteArrayId = id)
            )
            jobHandlerType = EngineTaskCommandsSendingJobHandler.TYPE
            duedate = Date.from(Instant.now())
            // we don't want to retry the sending.
            setRetriesFromPersistence(1)

            // this doesn't work yet... create PR at camunda for doing so...
            // dependentEntities[id] = ResourceEntity::class.java // put the entity as dependent to this job.
          }
          // send / store job.
          logger.trace { "SENDER-TRACE: storing Camunda Job for sending messages: $job" }
          context.jobManager.send(job)
        }

      } else {
        logger.debug { "SENDER-004: Process task sending is disabled by property. Would have sent $commands." }
      }
    }
  }
}
