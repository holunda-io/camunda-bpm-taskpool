package io.holunda.polyflow.view.jpa

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EntityScan
@EnableConfigurationProperties(PolyflowJpaViewProperties::class)
class PolyflowJpaViewConfiguration
