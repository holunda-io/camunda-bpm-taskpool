package io.holunda.camunda.taskpool.example.process.service

import mu.KLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
open class RequestPrinter(
  private val requestService: RequestService
) {

  companion object : KLogging()

  @Bean
  open fun showRequests(): ApplicationRunner {
    return ApplicationRunner {
      val requests = requestService.getAllRequests()
      logger.info("Found ${requests.size} requests.")
      requests.forEach {
        logger.info("$it")
        requestService.notify(it)
      }
    }
  }

}
