package io.holunda.camunda.taskpool.example.process.schedule

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Configuration
@EnableScheduling
@Profile("scheduled-processes")
class ScheduledConfiguration

@Component
class ScheduledStarter(
  private val processBean: ProcessApproveRequestBean
) {

  companion object : KLogging()

  @Value("\${scheduled-processes.limit:1000}")
  private var limit: Long = 999

  @Scheduled(initialDelay = 3000, fixedRate = 100)
  fun startProcess() {
    if (processBean.countInstances() < limit) {
      val started = processBean.startProcess("1", originator = "kermit")
      logger.info { "Successfully started process with $started" }
    }
  }

  @Scheduled(initialDelay = 30000, fixedRate = 20000)
  fun reportInstanceCount() {
    logger.info { "Currently running instances: ${processBean.countInstances()}" }
  }
}
