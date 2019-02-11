package io.holunda.camunda.taskpool.cockpit

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.reactive.config.EnableWebFlux

/**
 * Starts the app.
 */
fun main(args: Array<String>) {
  SpringApplication.run(TaskpoolCockpitApplication::class.java, *args)
}

@SpringBootApplication
@EnableTaskPoolCockpit
@EnableWebFlux
open class TaskpoolCockpitApplication
