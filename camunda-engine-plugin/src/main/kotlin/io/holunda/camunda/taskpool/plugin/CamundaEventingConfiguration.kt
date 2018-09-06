package io.holunda.camunda.taskpool.plugin

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

annotation class EnableCamundaEventingPlugin {

}

@ComponentScan
@Configuration
open class CamundaEventingConfiguration {

}
