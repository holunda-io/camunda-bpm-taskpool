package io.holunda.polyflow.example.tasklist.itest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.polyflow.example.tasklist.auth.CurrentUserService
import io.holunda.polyflow.example.tasklist.EnableTasklist
import io.holunda.polyflow.example.users.UserStoreService
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.FormUrlResolver
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.UnknownUserException
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.auth.UserService
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableTasklist
class ITestApplication {

  companion object {
    const val ITEST = "itest"
  }

  @Bean
  fun mockQueryGateway(): QueryGateway = mock()

  @Bean
  fun mockCommandGateway(): CommandGateway = mock()

  @Bean
  fun fakingCurrentUserService(): CurrentUserService = object : CurrentUserService {
    override fun getCurrentUser(): String = "current-user-for-the-test"
  }

  @Bean
  fun mockUserStoreService(): UserStoreService = mock()

  @Bean
  fun fakingUserService(): UserService = object : UserService {
    val users = mapOf("id1" to User("kermit", setOf("muppetshow")), "id2" to User("ironman", setOf("the avengers")))
    override fun getUser(userIdentifier: String): User {
      return users[userIdentifier] ?: throw UnknownUserException("No user with id $userIdentifier is found")
    }
  }

  @Bean
  fun formUrlResolver(): FormUrlResolver = object : FormUrlResolver {
    override fun resolveUrl(task: Task): String = "url"
    override fun resolveUrl(processDefinition: ProcessDefinition): String = "url2"
    override fun resolveUrl(dataEntry: DataEntry): String = "url3"
  }

  @Bean
  fun objectMapper(): ObjectMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}
