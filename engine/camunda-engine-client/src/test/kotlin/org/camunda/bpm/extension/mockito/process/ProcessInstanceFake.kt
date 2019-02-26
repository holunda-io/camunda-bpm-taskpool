package org.camunda.bpm.extension.mockito.process

import org.camunda.bpm.engine.runtime.ProcessInstance

// FIXME, remove as soon as #68 of camunda-bpm-mockito has been released (probably in version 4.0.1)
class ProcessInstanceFake(
  private val id: String,
  private val businessKey: String,
  private val processDefinitionId: String,
  private val processInstanceId: String?,
  private val tenantId: String?,
  private val caseInstanceId: String?,
  private var ended: Boolean = false,
  private var suspended: Boolean = false,
  private var rootProcessInstanceId: String?) : ProcessInstance {

  companion object {
    fun builder(): ProcessInstanceFakeBuilder {
      return ProcessInstanceFakeBuilder()
    }
  }

  override fun getRootProcessInstanceId(): String? {
    return rootProcessInstanceId
  }


  override fun getProcessDefinitionId(): String {
    return processDefinitionId
  }

  override fun getBusinessKey(): String {
    return businessKey
  }

  override fun getCaseInstanceId(): String? {
    return caseInstanceId
  }

  override fun getId(): String {
    return id
  }

  override fun isSuspended(): Boolean {
    return suspended
  }

  override fun isEnded(): Boolean {
    return ended
  }

  override fun getProcessInstanceId(): String? {
    return processInstanceId
  }

  override fun getTenantId(): String? {
    return tenantId
  }
}

class ProcessInstanceFakeBuilder {
  private var id: String? = null
  private var businessKey: String? = null
  private var processInstanceId: String? = null
  private var processDefinitionId: String? = null
  private var tenantId: String? = null
  private var caseInstanceId: String? = null
  private var ended = false
  private var suspended = false
  private var rootProcessInstanceId: String? = null

  fun id(id: String): ProcessInstanceFakeBuilder {
    this.id = id
    return this
  }

  fun businessKey(businessKey: String): ProcessInstanceFakeBuilder {
    this.businessKey = businessKey
    return this
  }

  fun processInstanceId(processInstanceId: String): ProcessInstanceFakeBuilder {
    this.processInstanceId = processInstanceId
    return this
  }

  fun processDefinitionId(processDefinitionId: String): ProcessInstanceFakeBuilder {
    this.processDefinitionId = processDefinitionId
    return this
  }

  fun tenantId(tenantId: String): ProcessInstanceFakeBuilder {
    this.tenantId = tenantId
    return this
  }

  fun rootProcessInstanceId(rootProcessInstanceId: String): ProcessInstanceFakeBuilder {
    this.rootProcessInstanceId = rootProcessInstanceId
    return this
  }

  fun caseInstanceId(caseInstanceId: String): ProcessInstanceFakeBuilder {
    this.caseInstanceId = caseInstanceId
    return this
  }

  fun ended(ended: Boolean): ProcessInstanceFakeBuilder {
    this.ended = ended
    return this
  }

  fun suspended(suspended: Boolean): ProcessInstanceFakeBuilder {
    this.suspended = suspended
    return this
  }

  override fun toString(): String {
    return "ProcessInstanceFakeBuilder{" +
      "id='" + id + '\''.toString() +
      ", businessKey='" + businessKey + '\''.toString() +
      ", processInstanceId='" + processInstanceId + '\''.toString() +
      ", processDefinitionId='" + processDefinitionId + '\''.toString() +
      ", tenantId='" + tenantId + '\''.toString() +
      ", caseInstanceId='" + caseInstanceId + '\''.toString() +
      ", ended=" + ended +
      ", suspended=" + suspended +
      ", rootProcessInstanceId=" + rootProcessInstanceId +
      '}'.toString()
  }

  fun build(): ProcessInstanceFake {
    return ProcessInstanceFake(
      id = id!!,
      businessKey = businessKey!!,
      processInstanceId = processInstanceId,
      processDefinitionId = processDefinitionId!!,
      tenantId = tenantId,
      caseInstanceId = caseInstanceId,
      ended = ended,
      suspended = suspended,
      rootProcessInstanceId = rootProcessInstanceId
    )
  }

}
