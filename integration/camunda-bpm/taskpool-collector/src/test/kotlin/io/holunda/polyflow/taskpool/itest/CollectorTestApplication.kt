package io.holunda.polyflow.taskpool.itest

import io.holunda.polyflow.taskpool.EnableCamundaTaskpoolCollector
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@SpringBootApplication
@EnableProcessApplication
@EnableCamundaTaskpoolCollector
class CollectorTestApplication {

  @Bean
  @Primary
  fun testAxonCommandGateway(): CommandGateway = mock()


  @Bean
  fun addCandidateUserPiggy() = TaskListener { delegateTask -> delegateTask.addCandidateUser("piggy") }

  @Bean
  fun setAssigneePiggy() = TaskListener { delegateTask -> delegateTask.assignee = "piggy" }


  @Bean
  fun addCandidateGroupMuppetShow() = TaskListener { delegateTask -> delegateTask.addCandidateGroup("muppetshow") }

  /**
   * Typical use case for a start listener changing attributes
   */
  @Bean
  fun changeTaskAttributes() = TaskListener { delegateTask ->
    delegateTask.name = "new name"
    delegateTask.description = "new description"
    delegateTask.priority = 99
    delegateTask.dueDate = TestDriver.NOW
    delegateTask.followUpDate = TestDriver.NOW
  }
}

