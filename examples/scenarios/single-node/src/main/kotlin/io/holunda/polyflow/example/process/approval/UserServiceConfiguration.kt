package io.holunda.polyflow.example.process.approval

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class UserServiceConfiguration {

  @Bean
  @Scope(scopeName = "prototype")
  fun currentUserStore(): CurrentUserStore {
    return CurrentUserStore()
  }
}
