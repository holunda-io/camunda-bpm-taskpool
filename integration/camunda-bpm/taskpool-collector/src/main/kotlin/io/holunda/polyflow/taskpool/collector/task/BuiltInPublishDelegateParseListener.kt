package io.holunda.polyflow.taskpool.collector.task

import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl
import org.camunda.bpm.engine.impl.task.TaskDefinition
import org.camunda.bpm.engine.impl.util.xml.Element
import org.camunda.bpm.spring.boot.starter.event.TaskEvent
import org.springframework.context.ApplicationEventPublisher

/**
 * Parse listener adding built-in task listeners only.
 */
class BuiltInPublishDelegateParseListener(
  publisher: ApplicationEventPublisher
) : AbstractBpmnParseListener() {

  companion object {
    private val TASK_EVENTS = listOf(
      TaskListener.EVENTNAME_COMPLETE,
      TaskListener.EVENTNAME_ASSIGNMENT,
      TaskListener.EVENTNAME_CREATE,
      TaskListener.EVENTNAME_DELETE,
      TaskListener.EVENTNAME_UPDATE
    )
  }

  private var taskListener: TaskListener = TaskListener { delegateTask: DelegateTask ->
    publisher.publishEvent(delegateTask)
    publisher.publishEvent(TaskEvent(delegateTask))
  }

  override fun parseUserTask(userTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
    addTaskListener(taskDefinition(activity))
  }

  override fun parseBoundaryErrorEventDefinition(
    errorEventDefinition: Element,
    interrupting: Boolean,
    activity: ActivityImpl,
    nestedErrorEventActivity: ActivityImpl
  ) {
    // Do not implement. Start and end event listener are set by parseBoundaryEvent()
  }

  override fun parseBoundaryEvent(boundaryEventElement: Element, scopeElement: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseBoundaryMessageEventDefinition(element: Element, interrupting: Boolean, activity: ActivityImpl) {
    // Do not implement. Start and end event listener are set by parseBoundaryEvent()
  }

  override fun parseBoundarySignalEventDefinition(signalEventDefinition: Element, interrupting: Boolean, activity: ActivityImpl) {
    // Do not implement. Start and end event listener are set by parseBoundaryEvent()
  }

  override fun parseBoundaryTimerEventDefinition(timerEventDefinition: Element, interrupting: Boolean, activity: ActivityImpl) {
    // Do not implement. Start and end event listener are set by parseBoundaryEvent()
  }

  override fun parseBusinessRuleTask(businessRuleTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseCallActivity(callActivityElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseCompensateEventDefinition(compensateEventDefinition: Element, activity: ActivityImpl) {
    // Do not implement. Start and end event listener are set by parseBoundaryEvent()
  }

  override fun parseEndEvent(endEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseEventBasedGateway(eventBasedGwElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseExclusiveGateway(exclusiveGwElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseInclusiveGateway(inclusiveGwElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseIntermediateCatchEvent(intermediateEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseIntermediateMessageCatchEventDefinition(messageEventDefinition: Element, activity: ActivityImpl) {
    // Do not implement. Start and end event listener are set by parseIntermediateCatchEvent()
  }

  override fun parseIntermediateSignalCatchEventDefinition(signalEventDefinition: Element, activity: ActivityImpl) {
    // Do not implement. Start and end event listener are set by parseIntermediateCatchEvent()
  }

  override fun parseIntermediateThrowEvent(intermediateEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseIntermediateTimerEventDefinition(timerEventDefinition: Element, activity: ActivityImpl) {
    // Do not implement. Start and end event listener are set by parseIntermediateCatchEvent()
  }

  override fun parseManualTask(manualTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseMultiInstanceLoopCharacteristics(activityElement: Element, multiInstanceLoopCharacteristicsElement: Element, activity: ActivityImpl) {
    // DO NOT IMPLEMENT
    // we do not notify on entering a multi-instance activity, this will be done for every single execution inside that loop.
  }

  override fun parseParallelGateway(parallelGwElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseProcess(processElement: Element, processDefinition: ProcessDefinitionEntity) {
  }

  override fun parseReceiveTask(receiveTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseScriptTask(scriptTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseSendTask(sendTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseSequenceFlow(sequenceFlowElement: Element, scopeElement: ScopeImpl, transition: TransitionImpl) {
  }

  override fun parseServiceTask(serviceTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseStartEvent(startEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseSubProcess(subProcessElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseTask(taskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  override fun parseTransaction(transactionElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
  }

  private fun addTaskListener(taskDefinition: TaskDefinition) {
    for (event in TASK_EVENTS) {
      taskDefinition.addBuiltInTaskListener(event, taskListener)
    }
  }

  /**
   * Retrieves task definition.
   *
   * @param activity the taskActivity
   * @return taskDefinition for activity
   */
  private fun taskDefinition(activity: ActivityImpl): TaskDefinition {
    val activityBehavior = activity.activityBehavior as UserTaskActivityBehavior
    return activityBehavior.taskDefinition
  }
}
