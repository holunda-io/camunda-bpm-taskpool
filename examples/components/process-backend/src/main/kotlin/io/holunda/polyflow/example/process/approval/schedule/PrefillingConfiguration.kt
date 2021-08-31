package io.holunda.polyflow.example.process.approval.schedule

import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcessBean
import io.holunda.polyflow.example.process.approval.service.RequestService
import io.holunda.polyflow.example.process.approval.service.createSalaryRequest
import mu.KLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Configuration

/**
 * This configuration makes sure that some processes are started directly
 * after empty system initialization to demonstrate how the system looks like
 * with some data.
 */
@Configuration
class PrefillingConfiguration(
  private val processApprovalProcessBean: RequestApprovalProcessBean,
  private val requestService: RequestService
) : ApplicationRunner {

  companion object : KLogging() {
    const val MIN_INSTANCES = 1L
  }

  override fun run(args: ApplicationArguments) {
    // only prefill if the system is empty.
    if (processApprovalProcessBean.countInstances() < MIN_INSTANCES) {
      logger.info { "Minimal process instance threshold ($MIN_INSTANCES) reached, starting a process." }
      // request for salary increase of Miss Piggy
      val username = "kermit"
      val requests = requestService.getAllDraftRequests(0)
      val request = if (requests.isNotEmpty()) {
        requests[0]
      } else {
        createSalaryRequest().apply {
          requestService.addRequest(this, username, 0)
        }
      }
      processApprovalProcessBean.startProcess(request.id, username)
    }
  }
}
