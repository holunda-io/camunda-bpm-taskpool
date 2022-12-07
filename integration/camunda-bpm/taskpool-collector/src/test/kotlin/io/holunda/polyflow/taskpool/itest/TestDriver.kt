package io.holunda.polyflow.taskpool.itest

import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.UpdateAttributeTaskCommand
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import java.time.Instant
import java.util.*

class TestDriver(
  private val repositoryService: RepositoryService,
  private val runtimeService: RuntimeService
) {


  companion object {
    val NOW = Date.from(Instant.now())
    val BUSINESS_KEY = "BK" + UUID.randomUUID().toString()
    val PROCESS_ID = "process-" + UUID.randomUUID().toString()
    val TASK_DEFINITION_KEY = "task-" + UUID.randomUUID().toString()
    val DEFAULT_VARIABLES = Variables.createVariables().apply { put("key", "value") }


    /**
     * Creates a process model instance with start -> user-task -> (optional: another-user-task) -> end
     */
    fun createUserTaskProcess(
      processId: String = this.PROCESS_ID,
      taskDefinitionKey: String = this.TASK_DEFINITION_KEY,
      additionalUserTask: Boolean = false,
      asyncOnStart: Boolean = false,
      candidateGroups: String = "",
      candidateUsers: String = "",
      formKey: String = "form-key",
      taskListeners: List<Pair<String, String>> = listOf(),
      otherTaskDefinitionKey: String = "another-user-task"
    ): BpmnModelInstance = Bpmn
      .createExecutableProcess(processId)
      // start event
      .startEvent("start").camundaAsyncAfter(asyncOnStart)
      // user task
      .userTask(taskDefinitionKey)
      .camundaCandidateGroups(candidateGroups)
      .camundaCandidateUsers(candidateUsers)
      .camundaFormKey(formKey)
      .camundaPriority("66")
      .apply {
        taskListeners.forEach {
          this.camundaTaskListenerDelegateExpression(it.first, it.second)
        }
        this.element.name = "User Task"
      }
      // optional second user task
      .apply {
        if (additionalUserTask) {
          this.userTask(otherTaskDefinitionKey)
        }
      }
      // end event
      .endEvent("end")
      .done().apply {
        getModelElementById<ModelElementInstance>(processId).setAttributeValue("name", "My Process")
      }


    /*
 * Create task command from current task.
 */
    fun createTaskCommand(
      candidateGroups: Set<String> = setOf(),
      candidateUsers: Set<String> = setOf(),
      variables: VariableMap = DEFAULT_VARIABLES,
      processBusinessKey: String = BUSINESS_KEY
    ) = BpmnAwareTests.task(BpmnAwareTests.taskQuery().initializeFormKeys()).let { task ->
      CreateTaskCommand(
        id = task.id,
        sourceReference = ProcessReference(
          instanceId = task.processInstanceId,
          executionId = task.executionId,
          definitionId = task.processDefinitionId,
          name = "My Process",
          definitionKey = PROCESS_ID,
          applicationName = "collector-test"
        ),
        name = task.name,
        description = task.description,
        taskDefinitionKey = task.taskDefinitionKey,
        candidateUsers = candidateUsers,
        candidateGroups = candidateGroups,
        assignee = task.assignee,
        enriched = true,
        eventName = CamundaTaskEventType.CREATE,
        createTime = task.createTime,
        businessKey = processBusinessKey,
        priority = task.priority, // default by camunda if not set in explicit
        payload = variables,
        formKey = task.formKey
      )
    }

    /*
     * Creates update command from current task.
     */
    fun updateTaskCommand(
      variables: VariableMap = DEFAULT_VARIABLES,
      instanceBusinessKey: String = BUSINESS_KEY,
      correlations: VariableMap = newCorrelations()
    ) =
      BpmnAwareTests.task(BpmnAwareTests.taskQuery().initializeFormKeys()).let { task ->
        UpdateAttributeTaskCommand(
          id = task.id,
          name = task.name,
          description = task.description,
          dueDate = task.dueDate,
          followUpDate = task.followUpDate,
          owner = task.owner,
          priority = task.priority,
          taskDefinitionKey = task.taskDefinitionKey,
          sourceReference = ProcessReference(
            instanceId = task.processInstanceId,
            executionId = task.executionId,
            definitionId = task.processDefinitionId,
            name = "My Process",
            definitionKey = PROCESS_ID,
            applicationName = "collector-test"
          ),
          enriched = true,
          businessKey = instanceBusinessKey,
          payload = variables,
          correlations = correlations
        )
      }

  }

  fun assertProcessInstanceWaitsInUserTask(instance: ProcessInstance) {
    BpmnAwareTests.assertThat(instance).isWaitingAt(TASK_DEFINITION_KEY)
  }

  /*
   * Deploys the process.
   */
  fun deployProcess(modelInstance: BpmnModelInstance) {
    repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn", modelInstance)
      .deploy()
  }

  /*
   * Starts the process.
   */
  fun startProcessInstance(
    processId: String = Companion.PROCESS_ID,
    businessKey: String = Companion.BUSINESS_KEY,
    variables: VariableMap = DEFAULT_VARIABLES
  ) = runtimeService
    .startProcessInstanceByKey(
      processId,
      businessKey,
      variables
    ).also { instance ->
      BpmnAwareTests.assertThat(instance).isNotNull
      BpmnAwareTests.assertThat(instance).isStarted
    }
}