package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.impl.VariableMapImpl

/**
 * Module to map camunda variable impl.
 */
class VariableMapTypeMappingModule : SimpleModule() {
  init {
    addAbstractTypeMapping(VariableMap::class.java, VariableMapImpl::class.java)
  }
}
