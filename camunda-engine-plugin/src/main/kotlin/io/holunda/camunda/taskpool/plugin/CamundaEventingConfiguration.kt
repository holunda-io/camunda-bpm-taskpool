package io.holunda.camunda.taskpool.plugin

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan
@Configuration
@EnableConfigurationProperties(CamundaEventingProperties::class)
open class CamundaEventingConfiguration {

}
