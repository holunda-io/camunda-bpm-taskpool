package io.holunda.camunda.taskpool.itest

import io.holunda.camunda.taskpool.EnableCamundaTaskpoolCollector
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
@EnableProcessApplication
@EnableCamundaTaskpoolCollector
class CollectorTestApplication
