package io.holunda.camunda.taskpool

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

fun ProcessDefinitionEntity.candidateLinks(): List<IdentityLinkEntity> = this.identityLinks.filter { it.type == "candidate" }
/**
 * Retrieves a set of candidate user ids allowed to start given process definition.
 */
fun ProcessDefinitionEntity.candidateUsers() = this.candidateLinks().filter { it.isUser }.map { it.userId }.toSet()

/**
 * Retrieves a set of candidate group ids allowed to start given process definition.
 */
fun ProcessDefinitionEntity.candidateGroups() = this.candidateLinks().filter { it.isGroup }.map { it.groupId }.toSet()

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
