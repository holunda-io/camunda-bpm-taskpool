package io.holunda.polyflow.bus.jackson.annotation

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.ConfigurationCondition
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.util.ObjectUtils
import java.lang.invoke.MethodHandles
import java.util.stream.Stream

/**
 * Insired by org.axonframework.springboot.util.AbstractQualifiedBeanCondition
 * Abstract implementations for conditions that match against the availability of beans of a specific type with a
 * given qualifier.

 * Initialize the condition, looking for properties on a given annotation
 *
 * @param annotationName     The fully qualified class name of the annotation to find attributes on.
 * @param beanClassAttribute The attribute containing the bean class.
 * @param qualifierAttribute The attribute containing the qualifier.
 */
abstract class AbstractQualifiedBeanCondition(
  private val annotationName: String,
  private val beanClassAttribute: String,
  private val qualifierAttribute: String
) : SpringBootCondition(), ConfigurationCondition {

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
  }

  override fun getConfigurationPhase(): ConfigurationPhase {
    return ConfigurationPhase.REGISTER_BEAN
  }

  override fun getMatchOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
    val annotationAttributes = metadata.getAllAnnotationAttributes(annotationName, true)
    val beanType = annotationAttributes!!.getFirst(beanClassAttribute) as String?
    val qualifierAttr = annotationAttributes.getFirst(qualifierAttribute) as String?
    val qualifier: String?
    val qualifierMatch: Boolean
    if (qualifierAttr!!.startsWith("!")) {
      qualifier = qualifierAttr.substring(1)
      qualifierMatch = false
    } else {
      qualifier = qualifierAttr
      qualifierMatch = true
    }
    val qualifiers = qualifier.split(",".toRegex()).toTypedArray()
    val conditionalClass: Class<*> = try {
      Class.forName(beanType)
    } catch (e: ClassNotFoundException) {
      val failureMessage = String.format(
        "Failed to extract a class instance for fully qualified class name [%s]",
        beanType
      )
      logger.warn(failureMessage, e)
      return ConditionOutcome(false, failureMessage)
    }
    val bf = context.beanFactory
    val anyMatch = Stream.of(*bf!!.getBeanNamesForType(conditionalClass))
      .anyMatch { beanName: String -> qualifierMatch == isOneMatching(beanName, bf, qualifiers) }
    val message = if (anyMatch) String.format(
      "Match found for class [%s] and qualifier [%s]",
      conditionalClass,
      qualifier
    ) else String.format("No match found for class [%s] and qualifier [%s]", conditionalClass, qualifier)
    return buildOutcome(anyMatch, message)
  }

  private fun isOneMatching(beanName: String, bf: ConfigurableListableBeanFactory, qualifiers: Array<String>): Boolean {
    for (qualifier in qualifiers) {
      if (isQualifierMatch(beanName, bf, qualifier)) {
        return true
      }
    }
    return false
  }

  protected abstract fun buildOutcome(anyMatch: Boolean, message: String): ConditionOutcome

}

/**
 * Inspired by org.axonframework.spring.SpringUtils.isQualifierMatch
 */
fun isQualifierMatch(beanName: String, beanFactory: ConfigurableListableBeanFactory, qualifier: String): Boolean {
  return if (!beanFactory.containsBean(beanName)) {
    false
  } else {
    try {
      val bd = beanFactory.getMergedBeanDefinition(beanName)
      if (bd is AnnotatedBeanDefinition) {
        val factoryMethodMetadata = bd.factoryMethodMetadata
        val qualifierAttributes = factoryMethodMetadata!!.getAnnotationAttributes(Qualifier::class.java.name)
        if (qualifierAttributes != null && qualifier == qualifierAttributes["value"]) {
          return true
        }
      }
      if (bd is AbstractBeanDefinition) {
        val candidate = bd.getQualifier(Qualifier::class.java.name)
        if (candidate != null && qualifier == candidate.getAttribute("value") || qualifier == beanName || ObjectUtils.containsElement(
            beanFactory.getAliases(
              beanName
            ), qualifier
          )
        ) {
          return true
        }
      }
      var targetAnnotation: Qualifier?
      if (bd is RootBeanDefinition) {
        val factoryMethod = bd.resolvedFactoryMethod
        if (factoryMethod != null) {
          targetAnnotation = AnnotationUtils.getAnnotation(factoryMethod, Qualifier::class.java)
          if (targetAnnotation != null) {
            return qualifier == targetAnnotation.value
          }
        }
      }
      val beanType = beanFactory.getType(beanName)
      if (beanType != null) {
        targetAnnotation = AnnotationUtils.getAnnotation(beanType, Qualifier::class.java)
        if (targetAnnotation != null) {
          return qualifier == targetAnnotation.value
        }
      }
    } catch (_: NoSuchBeanDefinitionException) {
    }
    false
  }
}

