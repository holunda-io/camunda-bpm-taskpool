package io.holunda.camunda.taskpool.collector.properties

import io.holunda.camunda.taskpool.TaskCollectorProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(TaskCollectorProperties::class)
open class TestApplication
