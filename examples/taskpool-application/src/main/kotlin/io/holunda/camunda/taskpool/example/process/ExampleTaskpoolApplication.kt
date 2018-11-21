package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.datapool.core.EnableDataPool
import io.holunda.camunda.taskpool.cockpit.EnableTaskPoolCockpit
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.example.tasklist.EnableTasklist
import io.holunda.camunda.taskpool.view.simple.EnableTaskPoolSimpleView
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.reactive.config.EnableWebFlux


fun main(args: Array<String>) {
  SpringApplication.run(ExampleTaskpoolApplication::class.java, *args)
}

@SpringBootApplication
@EnableTaskPool
@EnableDataPool
@EnableTaskPoolSimpleView
@EnableTasklist
@EnableWebFlux
@EnableTaskPoolCockpit
open class ExampleTaskpoolApplication {

}


