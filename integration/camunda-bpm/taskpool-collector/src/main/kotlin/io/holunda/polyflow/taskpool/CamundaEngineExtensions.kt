package io.holunda.polyflow.taskpool

import org.camunda.bpm.engine.context.ProcessEngineContext
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity

/**
 * Runs a command in command context.
 */
fun <T> ProcessEngineConfigurationImpl.executeInCommandContext(command: Command<T>): T {
  return this.commandExecutorTxRequired.execute(command)
}
/**
 * Execute call in a process engine context.
 * @param newContext if set to true, the new context is created.
 * @param call function to be called inside the process engine context.
 * @return T return value of the function.
 */
fun <T : Any> callInProcessEngineContext(newContext: Boolean, call: () -> T): T {
  return if (newContext) {
    try {
      ProcessEngineContext.requiresNew();
      call.invoke()
    } finally {
      ProcessEngineContext.clear();
    }
  } else {
    call.invoke()
  }
}
