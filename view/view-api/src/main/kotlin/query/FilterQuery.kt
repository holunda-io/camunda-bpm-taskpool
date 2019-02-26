package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.api.business.DataIdentity

interface FilterQuery<T: Any> {
  fun applyFilter(element: T): Boolean
}
