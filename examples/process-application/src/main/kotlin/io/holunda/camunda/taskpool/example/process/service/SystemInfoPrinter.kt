package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.taskpool.example.users.UserStoreService
import mu.KLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SystemInfoPrinter(
  private val requestService: RequestService,
  private val userStoreService: UserStoreService
) {

  companion object : KLogging()

  @Bean
  fun requestPrinter(): ApplicationRunner {
    return ApplicationRunner {
      val requests = requestService.getAllRequests()
      logger.info("Found ${requests.size} requests.")
      requests.forEach {
        logger.info("Request $it")
      }
    }
  }

  @Bean
  fun userPrinter(): ApplicationRunner {
    return ApplicationRunner {
      val users = userStoreService.getUsers()
      logger.info { "Found ${users.size} users." }
      users.forEach {
        logger.info { "User $it" }
      }
    }
  }

}
