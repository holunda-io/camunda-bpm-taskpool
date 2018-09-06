package io.holunda.camunda.taskpool.plugin

import mu.KLogging
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.ExecutionListener.*
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.delegate.TaskListener.*
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl
import org.camunda.bpm.engine.impl.task.TaskDefinition
import org.camunda.bpm.engine.impl.util.xml.Element
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
open class CamundaEventingEnginePlugin(private val publisher: ApplicationEventPublisher) : SpringBootProcessEnginePlugin() {

  companion object : KLogging()

  private val taskEvents = arrayOf(
      EVENTNAME_COMPLETE,
      EVENTNAME_ASSIGNMENT,
      EVENTNAME_CREATE,
      EVENTNAME_DELETE)
  private val executionEvents = arrayOf(
      EVENTNAME_START,
      EVENTNAME_END)


  private val taskListener = TaskListener { t -> publisher.publishEvent(t) }
  private val executionListener = ExecutionListener { e -> publisher.publishEvent(e) }

  override fun preInit(processEngineConfiguration: SpringProcessEngineConfiguration) {
    logger.info("Initialized Camunda Eventing Engine Plugin. All Camunda Events are now available as Spring events.")
    processEngineConfiguration.customPostBPMNParseListeners.add(PublishDelegateParseListener())
  }

  inner class PublishDelegateParseListener : AbstractBpmnParseListener() {

    override fun parseUserTask(userTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addTaskListener(taskDefinition(activity))
      addExecutionListener(activity)
    }

    override fun parseBoundaryErrorEventDefinition(errorEventDefinition: Element, interrupting: Boolean, activity: ActivityImpl, nestedErrorEventActivity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseBoundaryEvent(boundaryEventElement: Element, scopeElement: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseBoundaryMessageEventDefinition(element: Element, interrupting: Boolean, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseBoundarySignalEventDefinition(signalEventDefinition: Element, interrupting: Boolean, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseBoundaryTimerEventDefinition(timerEventDefinition: Element, interrupting: Boolean, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseBusinessRuleTask(businessRuleTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseCallActivity(callActivityElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseCompensateEventDefinition(compensateEventDefinition: Element, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseEndEvent(endEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseEventBasedGateway(eventBasedGwElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseExclusiveGateway(exclusiveGwElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseInclusiveGateway(inclusiveGwElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseIntermediateCatchEvent(intermediateEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseIntermediateMessageCatchEventDefinition(messageEventDefinition: Element, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseIntermediateSignalCatchEventDefinition(signalEventDefinition: Element, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseIntermediateThrowEvent(intermediateEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseIntermediateTimerEventDefinition(timerEventDefinition: Element, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseManualTask(manualTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseMultiInstanceLoopCharacteristics(activityElement: Element, multiInstanceLoopCharacteristicsElement: Element, activity: ActivityImpl) {
      // DO NOT IMPLEMENT!
      // we do not notify on entering a multi-instance activity, this will be done for every single execution inside that loop.
    }

    override fun parseParallelGateway(parallelGwElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseProcess(processElement: Element, processDefinition: ProcessDefinitionEntity) {
      // is it a good idea to implement generic global process listeners?
    }

    override fun parseReceiveTask(receiveTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseScriptTask(scriptTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseSendTask(sendTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseSequenceFlow(sequenceFlowElement: Element, scopeElement: ScopeImpl, transition: TransitionImpl) {
      addExecutionListener(transition)
    }

    override fun parseServiceTask(serviceTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseStartEvent(startEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseSubProcess(subProcessElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseTask(taskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    override fun parseTransaction(transactionElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
      addExecutionListener(activity)
    }

    private fun addExecutionListener(activity: ActivityImpl) {
      for (event in executionEvents) {
        activity.addListener(event, executionListener)
      }
    }

    private fun addExecutionListener(transition: TransitionImpl) {
      transition.addListener(EVENTNAME_TAKE, executionListener)
    }

    private fun addTaskListener(taskDefinition: TaskDefinition) {
      for (event in taskEvents) {
        taskDefinition.addTaskListener(event, taskListener)
      }
    }

    /**
     * @param activity the taskActivity
     * @return taskDefinition for activity
     */
    private fun taskDefinition(activity: ActivityImpl): TaskDefinition {
      val activityBehavior = activity.activityBehavior as UserTaskActivityBehavior
      return activityBehavior.taskDefinition
    }
  }


}
