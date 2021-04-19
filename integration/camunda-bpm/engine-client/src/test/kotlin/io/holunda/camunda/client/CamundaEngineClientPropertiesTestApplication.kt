package io.holunda.camunda.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(CamundaEngineClientProperties::class)
class CamundaEngineClientPropertiesTestApplication {

}
