package io.holunda.camunda.taskpool.example.process.schedule

import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcessBean
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
import java.util.concurrent.atomic.AtomicLong


@Configuration
@EnableScheduling
@Profile("scheduled-processes")
class ScheduledConfiguration

@Component
class ScheduledStarter(
  private val processApprovalProcessBean: RequestApprovalProcessBean,
  private val requestService: RequestService,
  private val userStoreService: UserStoreService
) {

  companion object : KLogging()

  @Value("\${scheduled-processes.limit:1000}")
  private var limit: Long = 999
  private lateinit var requestInfo: Pair<String, AtomicLong>

  @Scheduled(initialDelay = 2000, fixedRate = Integer.MAX_VALUE.toLong())
  fun saveDummyRequest() {
    val username = userStoreService.getUsers().map{
      val revision = 1L
      this.requestInfo = requestService.addRequest(createDummyRequest(), it.username, revision) to AtomicLong(revision)
    }
  }

  @Scheduled(initialDelay = 3000, fixedRate = 100)
  fun startProcess() {
    if (processApprovalProcessBean.countInstances() < limit) {
      val started = processApprovalProcessBean.startProcess(
        requestId = this.requestInfo.first,
        originator = userStoreService.getUsers()[0].username,
        revision = this.requestInfo.second.incrementAndGet()
      )
      logger.info { "Successfully started process with $started" }
    }
  }

  @Scheduled(initialDelay = 30000, fixedRate = 20000)
  fun reportInstanceCount() {
    logger.info { "Currently running instances: ${processApprovalProcessBean.countInstances()}" }
  }
}
