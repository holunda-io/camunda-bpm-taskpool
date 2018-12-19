package io.holunda.camunda.taskpool.itest

import io.holunda.camunda.taskpool.EnableTaskCollector
import io.holunda.camunda.taskpool.plugin.EnableCamundaSpringEventing
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
@EnableProcessApplication
@EnableCamundaSpringEventing
@EnableTaskCollector
open class TaskCollectorTestApplication {

}

