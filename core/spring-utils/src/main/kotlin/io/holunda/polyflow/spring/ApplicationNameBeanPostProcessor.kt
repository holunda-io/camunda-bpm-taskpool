package io.holunda.polyflow.spring

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor.Companion.UNSET_APPLICATION_NAME
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.env.get
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter

/**
 * A bean post processor that inspects all [ConfigurationProperties] beans, looks for a mutable property of type `String` called `applicationName` and replaces it with the value
 * of `spring.application.name` if its current value is [UNSET_APPLICATION_NAME].
 */
class ApplicationNameBeanPostProcessor(private val applicationContext: ApplicationContext) : BeanPostProcessor {

  companion object {
    const val UNSET_APPLICATION_NAME = "unset-application-name"
    val CANDIDATES = arrayOf(
      "io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties",
      "io.holunda.polyflow.client.camunda.CamundaEngineClientProperties",
      "io.holunda.polyflow.datapool.DataEntrySenderProperties",
    )
  }

  private val applicationName: String by lazy { applicationContext.environment["spring.application.name"] ?: UNSET_APPLICATION_NAME }
  override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
    if (CANDIDATES.contains(bean::class.java.name)
      && AnnotationUtils.findAnnotation(bean::class.java, ConfigurationProperties::class.java) != null) {
      @Suppress("UNCHECKED_CAST")
      val applicationNameProperty =
        bean::class.memberProperties.find { it.name == "applicationName" && it is KMutableProperty1<out Any, *> && it.returnType.classifier == String::class } as KMutableProperty1<Any, String>?
      if ( applicationNameProperty != null) {
        if (applicationNameProperty.get(bean) == UNSET_APPLICATION_NAME) {
          applicationNameProperty.set(bean, applicationName)
        }
      }
    }
    return bean
  }
}
