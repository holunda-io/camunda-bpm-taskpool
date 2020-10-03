package io.holunda.camunda.taskpool.gateway

import org.axonframework.queryhandling.QueryBus
import org.axonframework.queryhandling.QueryGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(RevisionAwareQueryGatewayProperties::class)
@ConditionalOnProperty(prefix = "camunda.taskpool.gateway", name = ["type"], value = ["revision-aware"])
class RevisionAwareQueryGatewayConfiguration {

  @Bean
  fun revisionAwareGateway(queryBus: QueryBus, properties: RevisionAwareQueryGatewayProperties): QueryGateway {
    return RevisionAwareQueryGateway(queryBus, properties.defaultQueryTimeout)
  }

}


