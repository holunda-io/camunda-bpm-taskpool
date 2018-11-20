package io.holunda.camunda.taskpool.plugin

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component

@EnableProcessApplication
@SpringBootApplication
@EnableCamundaSpringEventing
open class TestApplication {

}

@Component
class ServiceTask : JavaDelegate {
  override fun execute(execution: DelegateExecution) {
  }
}
