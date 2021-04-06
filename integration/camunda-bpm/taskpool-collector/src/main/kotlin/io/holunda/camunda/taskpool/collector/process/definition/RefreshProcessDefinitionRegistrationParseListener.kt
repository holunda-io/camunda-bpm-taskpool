package io.holunda.camunda.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.executeInCommandContext
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.impl.util.xml.Element

/**
 * Parse listener that starts a refresh process definition job on every new process parse (deployment).
 */
class RefreshProcessDefinitionRegistrationParseListener(
  private val processEngineConfiguration: ProcessEngineConfigurationImpl
) : AbstractBpmnParseListener() {

  override fun parseProcess(processElement: Element, processDefinition: ProcessDefinitionEntity) {
    // create job / send command to job executor
    // to handle this deployment asynchronous.
    processEngineConfiguration.executeInCommandContext(RefreshProcessDefinitionsJobCommand(processDefinitionKey = processDefinition.key))
  }
}
