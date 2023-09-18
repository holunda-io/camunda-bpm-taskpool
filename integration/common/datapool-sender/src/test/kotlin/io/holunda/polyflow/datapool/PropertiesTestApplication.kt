package io.holunda.polyflow.datapool

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@SpringBootApplication
@EnableConfigurationProperties(DataEntrySenderProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class PropertiesTestApplication
