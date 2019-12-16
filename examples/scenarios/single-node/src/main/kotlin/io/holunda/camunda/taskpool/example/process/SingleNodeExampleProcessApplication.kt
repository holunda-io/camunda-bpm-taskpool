package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.datapool.core.EnableDataPool
import io.holunda.camunda.taskpool.EnableTaskpoolEngineSupport
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.example.tasklist.EnableTasklist
import io.holunda.camunda.taskpool.example.users.EnableExampleUsers
import io.holunda.camunda.taskpool.urlresolver.EnablePropertyBasedFormUrlResolver
import io.holunda.camunda.taskpool.view.simple.EnableTaskPoolSimpleView
import mu.KLogging
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


fun main(args: Array<String>) {
  SpringApplication.run(SingleNodeExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableExampleUsers
@EnableTaskPoolSimpleView
@EnableTaskPool
@EnableDataPool
@EnableTasklist
@EnablePropertyBasedFormUrlResolver
class SingleNodeExampleProcessApplication {

  companion object : KLogging()

}
