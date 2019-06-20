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
    Request(id = "1", subject = "Salary increase", applicant = "hulk", amount = BigDecimal.valueOf(1000),  currency = "GBP"),
    Request(id = "2", subject = "Advanced training", applicant = "hulk", amount = BigDecimal.valueOf(500),  currency = "EUR"),
    Request(id = "3", subject = "Sabbatical", applicant = "ironman", amount = BigDecimal.valueOf(0),  currency = "USD"),
    Request(id = "4", subject = "Holiday trip", applicant = "inronman", amount = BigDecimal.valueOf(83.12), currency =  "USD")
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
