package io.holunda.polyflow.example.process.approval.service

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holunda.polyflow.example.users.UserStoreService
import io.holunda.polyflow.view.ProcessInstanceState
import io.holunda.polyflow.view.query.process.ProcessInstanceQueryResult
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
import mu.KLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SystemInfoPrinter(
  private val requestService: RequestService,
  private val userStoreService: UserStoreService,
  private val queryGateway: QueryGateway
) {

  companion object : KLogging()

  @Bean
  fun requestPrinter(): ApplicationRunner {
    return ApplicationRunner {
      val requests = requestService.getAllRequests(1)
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

  @Bean
  fun processInstancePrinter(): ApplicationRunner {
    return ApplicationRunner {
      val subscription = queryGateway.subscriptionQuery(
        ProcessInstancesByStateQuery(setOf(ProcessInstanceState.RUNNING, ProcessInstanceState.SUSPENDED, ProcessInstanceState.FINISHED)),
        QueryResponseMessageResponseType.queryResponseMessageResponseType<ProcessInstanceQueryResult>(),
        QueryResponseMessageResponseType.queryResponseMessageResponseType<ProcessInstanceQueryResult>()
      )

      subscription
        .initialResult()
        .concatWith(subscription.updates())
        .doOnError {
          logger.error(it) { "Error received listing process instances" }
        }
        .subscribe { result ->
          result.elements.forEach {
            logger.info { "Process instance ${it.processInstanceId} (${it.businessKey}) is ${it.state}." }
          }
        }
    }

  }

}
