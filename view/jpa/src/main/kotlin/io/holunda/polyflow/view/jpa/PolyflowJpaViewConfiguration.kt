package io.holunda.polyflow.view.jpa

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableConfigurationProperties(PolyflowJpaViewProperties::class)
@ComponentScan
class PolyflowJpaViewConfiguration
