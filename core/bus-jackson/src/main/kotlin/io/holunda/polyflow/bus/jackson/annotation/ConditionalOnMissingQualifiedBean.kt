package io.holunda.polyflow.bus.jackson.annotation

import org.springframework.context.annotation.Conditional
import kotlin.reflect.KClass

/**
 * Inspired by org.axonframework.springboot.util.ConditionalOnMissingQualifiedBean
 *
 * {@link Conditional} that only matches when for the specified bean class in the {@link BeanFactory} there is an
 * instance which has the given {@code qualifier} set on it.
 * <p>
 * The condition can only match the bean definitions that have been processed by the
 * application context so far and, as such, it is strongly recommended to use this
 * condition on auto-configuration classes only. If a candidate bean may be created by
 * another auto-configuration, make sure that the one using this condition runs after.
 *
 * @author Steven van Beelen
 */
@Target(
  AnnotationTarget.ANNOTATION_CLASS,
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(
  OnMissingQualifiedBeanCondition::class
)
annotation class ConditionalOnMissingQualifiedBean(
  /**
   * The class type of bean that should be checked. The condition matches if the class specified is contained in the
   * [ApplicationContext], together with the specified `qualifier`.
   */
  val beanClass: KClass<*> = Any::class,
  /**
   * The qualifier which all instances of the given {code beanClass} in the [ApplicationContext] will be matched
   * for. One may indicate that a qualifier should *not* be present by prefixing it with `!`, e.g:
   * `qualifier = "!unqualified"`.
   *
   *
   * Multiple qualifiers may be provided, separated with a comma (`,`). In that case, a bean matches when it is
   * assigned one of the given qualifiers.
   */
  val qualifier: String
)
