package io.holunda.camunda.taskpool.example.process.auth

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.users.UserStoreService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Configuration
class UserServiceConfiguration {

  companion object {
    const val NAME = "currentUserStore"
  }

  @Bean(name = [NAME])
  @Scope(scopeName = "prototype")
  fun currentUserStore(): CurrentUserStore {
    return CurrentUserStore()
  }
}

/**
 * Prototype bean, will be created for every request.
 */
@Component
class SimpleCurrentUserService(private val userStoreService: UserStoreService) : CurrentUserService {

  @Autowired
  private lateinit var currentUserStore: CurrentUserStore

  override fun getCurrentUser(): String = currentUserStore.username ?: throw IllegalArgumentException("No current user set")
}


/**
 * Hold the username
 */
data class CurrentUserStore(var username: String? = null) {
  fun clear() {
    this.username = null
  }
}
