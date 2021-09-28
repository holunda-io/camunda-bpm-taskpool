package io.holunda.polyflow.taskpool

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.DataEntryStateImpl
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.impl.VariableMapImpl

/**
 * Configures object mapper.
 */
fun ObjectMapper.configureTaskpoolJacksonObjectMapper(): ObjectMapper = this
  .registerModule(variableMapModule)
  .registerModule(dataEntryModule)
  .apply {
    addMixIn(SourceReference::class.java, KotlinTypeInfo::class.java)
  }

/**
 * Module to map camunda variable impl.
 */
val variableMapModule = SimpleModule()
  .apply {
    addAbstractTypeMapping(VariableMap::class.java, VariableMapImpl::class.java)
  }

/**
 * Module to map data entry state.
 */
val dataEntryModule = SimpleModule()
  .apply {
    addAbstractTypeMapping(DataEntryState::class.java, DataEntryStateImpl::class.java)
  }

/**
 * Type info for all classes using inheritance.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class", include = JsonTypeInfo.As.PROPERTY)
class KotlinTypeInfo
