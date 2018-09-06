package io.holunda.camunda.taskpool.plugin

import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl
import org.camunda.bpm.engine.impl.task.TaskDefinition
import org.camunda.bpm.engine.impl.util.xml.Element
import org.springframework.context.ApplicationEventPublisher

class PublishDelegateParseListener(private val publisher: ApplicationEventPublisher) : AbstractBpmnParseListener() {

  companion object {
    val TASK_EVENTS = arrayOf(
      TaskListener.EVENTNAME_COMPLETE,
      TaskListener.EVENTNAME_ASSIGNMENT,
      TaskListener.EVENTNAME_CREATE,
      TaskListener.EVENTNAME_DELETE)
    val EXECUTION_EVENTS = arrayOf(
      ExecutionListener.EVENTNAME_START,
      ExecutionListener.EVENTNAME_END)

  }

  private val taskListener = TaskListener { t -> publisher.publishEvent(t) }
  private val executionListener = ExecutionListener { e -> publisher.publishEvent(e) }


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
    for (event in EXECUTION_EVENTS) {
      activity.addListener(event, executionListener)
    }
  }

  private fun addExecutionListener(transition: TransitionImpl) {
    transition.addListener(ExecutionListener.EVENTNAME_TAKE, executionListener)
  }

  private fun addTaskListener(taskDefinition: TaskDefinition) {
    for (event in TASK_EVENTS) {
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
