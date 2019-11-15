package io.holunda.camunda.taskpool.example.process.schedule

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.holunda.camunda.taskpool.example.process.service.RequestService
import io.holunda.camunda.taskpool.example.process.service.createDummyRequest
import io.holunda.camunda.taskpool.example.users.UserStoreService
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


@Configuration
@EnableScheduling
@Profile("scheduled-processes")
class ScheduledConfiguration

@Component
class ScheduledStarter(
  private val processBean: ProcessApproveRequestBean,
  private val requestService: RequestService,
  private val userStoreService: UserStoreService
) {

  companion object : KLogging()

  @Value("\${scheduled-processes.limit:1000}")
  private var limit: Long = 999
  private lateinit var requestId: String

  @PostConstruct
  fun saveDummyRequest() {
    val username = userStoreService.getUsers()[0].username
    this.requestId = requestService.addRequest(createDummyRequest(), username)
  }

  @Scheduled(initialDelay = 3000, fixedRate = 100)
  fun startProcess() {
    if (processBean.countInstances() < limit) {
      val started = processBean.startProcess(this.requestId, originator = userStoreService.getUsers()[0].username)
      logger.info { "Successfully started process with $started" }
    }
  }

  @Scheduled(initialDelay = 30000, fixedRate = 20000)
  fun reportInstanceCount() {
    logger.info { "Currently running instances: ${processBean.countInstances()}" }
  }
}
