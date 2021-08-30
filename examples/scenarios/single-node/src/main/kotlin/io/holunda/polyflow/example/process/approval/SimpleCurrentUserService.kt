package io.holunda.polyflow.example.process.approval

import io.holunda.polyflow.example.tasklist.auth.CurrentUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.stereotype.Component

/**
 * Prototype bean, will be created for every request.
 */
@Component
class SimpleCurrentUserService() : CurrentUserService {

  @Autowired
  lateinit var currentUserStore: CurrentUserStore

  override fun getCurrentUser(): String = currentUserStore.username ?: throw IllegalArgumentException("No current user set")
}


