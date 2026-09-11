package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.DataEntryStateImpl

/**
 * Module to map data entry state.
 */
class DataEntryStateTypeMappingModule: SimpleModule() {
  init {
    addAbstractTypeMapping(DataEntryState::class.java, DataEntryStateImpl::class.java)
  }
}
