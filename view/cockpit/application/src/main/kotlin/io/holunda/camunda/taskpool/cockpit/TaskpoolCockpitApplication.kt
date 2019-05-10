package io.holunda.camunda.taskpool.cockpit

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Starts the app.
 */
fun main(args: Array<String>) {
  SpringApplication.run(TaskpoolCockpitApplication::class.java, *args)
}

/**
 * Cockpit application.
 */
@SpringBootApplication
@EnableTaskPoolCockpit
class TaskpoolCockpitApplication
