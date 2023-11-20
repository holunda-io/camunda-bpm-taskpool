package io.holunda.polyflow.datapool

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "polyflow.test")
data class DataEntrySenderProperties(var applicationName: String = ApplicationNameBeanPostProcessor.UNSET_APPLICATION_NAME)
