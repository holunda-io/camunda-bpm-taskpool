package io.holunda.camunda.taskpool

import org.camunda.bpm.engine.repository.ProcessDefinition

public class ProcessDefinitionFake internal constructor(
  private val id: String?,
  private val category: String?,
  private val name: String?,
  private val key: String?,
  private val version: Int?,
  private val resourceName: String?,
  private val deploymentId: String?,
  private val diagramResourceName: String?,
  private val tenantId: String?,
  private val description: String?,
  private val hasStartForm: Boolean?,
  private var suspended: Boolean?,
  private val historyTimeToLive: Int?,
  private val versionTag: String?,
  private val startableInTasklist: Boolean = false
  ) : ProcessDefinition {

  override fun getId(): String? {
    return id
  }

  override fun getCategory(): String? {
    return category
  }

  override fun getName(): String? {
    return name
  }

  override fun getKey(): String? {
    return key
  }

  override fun getVersion(): Int {
    return version!!
  }

  override fun getResourceName(): String? {
    return resourceName
  }

  override fun getDeploymentId(): String? {
    return deploymentId
  }

  override fun getDiagramResourceName(): String? {
    return diagramResourceName
  }

  override fun getTenantId(): String? {
    return tenantId
  }

  override fun getHistoryTimeToLive(): Int? {
    return historyTimeToLive
  }

  override fun getDescription(): String? {
    return description
  }

  override fun hasStartFormKey(): Boolean {
    return hasStartForm!!
  }

  override fun isSuspended(): Boolean {
    return suspended!!
  }

  override fun isStartableInTasklist(): Boolean = startableInTasklist

  override fun getVersionTag(): String? {
    return versionTag
  }

  fun setSuspended(suspended: Boolean) {
    this.suspended = suspended
  }

  companion object {
    fun builder(): ProcessDefinitionFakeBuilder {
      return ProcessDefinitionFakeBuilder()
    }
  }
}

class ProcessDefinitionFakeBuilder {
  private var id: String? = null
  private var category: String? = null
  private var name: String? = null
  private var key: String? = null
  private var version = 0
  private var resourceName: String? = null
  private var deploymentId: String? = null
  private var diagramResourceName: String? = null
  private var tenantId: String? = null
  private var description: String? = null
  private var hasStartForm = false
  private var suspended = false
  private var historyTimeToLive = 0
  private var versionTag: String? = null

  fun id(id: String): ProcessDefinitionFakeBuilder {
    this.id = id
    return this
  }

  fun category(category: String): ProcessDefinitionFakeBuilder {
    this.category = category
    return this
  }

  fun name(name: String): ProcessDefinitionFakeBuilder {
    this.name = name
    return this
  }

  fun key(key: String): ProcessDefinitionFakeBuilder {
    this.key = key
    return this
  }

  fun version(version: Int): ProcessDefinitionFakeBuilder {
    this.version = version
    return this
  }

  fun resourceName(resourceName: String): ProcessDefinitionFakeBuilder {
    this.resourceName = resourceName
    return this
  }

  fun deploymentId(deploymentId: String): ProcessDefinitionFakeBuilder {
    this.deploymentId = deploymentId
    return this
  }

  fun diagramResourceName(diagramResourceName: String): ProcessDefinitionFakeBuilder {
    this.diagramResourceName = diagramResourceName
    return this
  }

  fun tenantId(tenantId: String): ProcessDefinitionFakeBuilder {
    this.tenantId = tenantId
    return this
  }

  fun description(description: String): ProcessDefinitionFakeBuilder {
    this.description = description
    return this
  }

  fun hasStartForm(hasStartForm: Boolean): ProcessDefinitionFakeBuilder {
    this.hasStartForm = hasStartForm
    return this
  }

  fun suspended(suspended: Boolean): ProcessDefinitionFakeBuilder {
    this.suspended = suspended
    return this
  }

  fun historyTimeToLive(historyTimeToLive: Int): ProcessDefinitionFakeBuilder {
    this.historyTimeToLive = historyTimeToLive
    return this
  }

  fun versionTag(versionTag: String): ProcessDefinitionFakeBuilder {
    this.versionTag = versionTag
    return this
  }

  override fun toString(): String {
    return "ProcessDefinitionFakeBuilder{" +
      "id='" + id + '\''.toString() +
      ", category='" + category + '\''.toString() +
      ", name='" + name + '\''.toString() +
      ", key='" + key + '\''.toString() +
      ", version=" + version +
      ", resourceName='" + resourceName + '\''.toString() +
      ", deploymentId='" + deploymentId + '\''.toString() +
      ", diagramResourceName='" + diagramResourceName + '\''.toString() +
      ", tenantId='" + tenantId + '\''.toString() +
      ", description='" + description + '\''.toString() +
      ", hasStartForm=" + hasStartForm +
      ", suspended=" + suspended +
      ", historyTimeToLive=" + historyTimeToLive +
      ", versionTag='" + versionTag + '\''.toString() +
      '}'.toString()
  }

  fun build(): ProcessDefinitionFake {
    return ProcessDefinitionFake(
      id, category, name, key, version, resourceName,
      deploymentId, diagramResourceName, tenantId,
      description, hasStartForm, suspended, historyTimeToLive,
      versionTag
    )
  }
}


