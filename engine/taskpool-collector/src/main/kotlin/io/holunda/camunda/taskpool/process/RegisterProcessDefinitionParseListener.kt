package io.holunda.camunda.taskpool.process

import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.impl.util.xml.Element

class RegisterProcessDefinitionParseListener(
  private val processEngineConfiguration: ProcessEngineConfigurationImpl
) : AbstractBpmnParseListener() {

  override fun parseProcess(processElement: Element, processDefinition: ProcessDefinitionEntity) {
    // create job / send command to job executor
    // to handle this deployment asynchronous.
    processEngineConfiguration.commandExecutorTxRequired.execute(RefreshProcessDefinitionsJobCommand(processDefinitionKey = processDefinition.key))
  }
}
