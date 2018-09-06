package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.taskpool.example.process.ProcessApproveRequest.Variables.ON_BEHALF
import io.holunda.camunda.taskpool.example.process.ProcessApproveRequest.Variables.REQUESTER
import io.holunda.camunda.taskpool.example.process.ProcessApproveRequest.Variables.SUBJECT
import io.holunda.camunda.taskpool.example.process.ProcessApproveRequest.Variables.TARGET
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.Variables
import org.springframework.stereotype.Component
import java.util.*

object ProcessApproveRequest {
  const val KEY = "process_approve_request"
  const val RESOURCE = "process_approve_request.bpmn"

  object Variables {
    const val REQUESTER = "requester"
    const val ON_BEHALF = "on-behalf-of"
    const val SUBJECT = "subject"
    const val TARGET = "target"
  }
}

@Component
class ProcessApproveRequestBean(private val runtimeService: RuntimeService) {

  fun startProcess(): ProcessInstance {
    return runtimeService.startProcessInstanceByKey(ProcessApproveRequest.KEY,
      "AR-${UUID.randomUUID().toString()}",
      Variables.createVariables()
        .putValue(REQUESTER, "kermit")
        .putValue(ON_BEHALF, "piggy")
        .putValue(SUBJECT, "Salary increase")
        .putValue(TARGET, "1,000,000.00 USD/Y")
    )
  }

  fun countInstances() = getAllInstancesQuery().count()

  fun deleteAllInstances() {
    getAllInstancesQuery().list().forEach{ runtimeService.deleteProcessInstance(it.processInstanceId, "Deleted by the mass deletion REST call")}
  }

  private fun getAllInstancesQuery() = runtimeService.createProcessInstanceQuery().processDefinitionKey(ProcessApproveRequest.KEY)
}

