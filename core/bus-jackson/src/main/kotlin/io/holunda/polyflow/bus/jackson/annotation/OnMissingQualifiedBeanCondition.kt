package io.holunda.polyflow.bus.jackson.annotation

import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

/**
 * Insipred by
 * [Condition] implementation to check for a bean instance of a specific class *and* a specific qualifier on it,
 * matching if no such bean can be found.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
class OnMissingQualifiedBeanCondition : AbstractQualifiedBeanCondition(ConditionalOnMissingQualifiedBean::class.java.name, "beanClass", "qualifier") {
  override fun buildOutcome(anyMatch: Boolean, message: String): ConditionOutcome {
    return ConditionOutcome(!anyMatch, message)
  }

}
