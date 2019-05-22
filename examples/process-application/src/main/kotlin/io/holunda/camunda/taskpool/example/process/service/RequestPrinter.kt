package io.holunda.camunda.taskpool.example.process.service

import mu.KLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal


@Configuration
class RequestPrinter(
  private val requestService: RequestService,
  private val currentUserService: SimpleUserService
) {

  companion object : KLogging()

  //
  val testRequests = listOf(
    Request("1", "Salary increase", "hulk", BigDecimal.valueOf(1000), "GBP"),
    Request("2", "Advanced training", "hulk", BigDecimal.valueOf(500), "EUR"),
    Request("3", "Sabbatical", "ironman", BigDecimal.valueOf(0), "USD"),
    Request("4", "Holiday trip", "inronman", BigDecimal.valueOf(83.12), "USD")
  )


  @Bean
  fun ensureRequestsAreInPlace(): ApplicationRunner {
    return ApplicationRunner {
      val requests = requestService.getAllRequests()
      logger.info("Found ${requests.size} requests.")
      if (requests.isEmpty()) {
        testRequests.forEach {
          logger.info("Adding request $it")
          requestService.addRequest(it, currentUserService.getAllUsers()[0])
        }
      }
    }
  }


}
