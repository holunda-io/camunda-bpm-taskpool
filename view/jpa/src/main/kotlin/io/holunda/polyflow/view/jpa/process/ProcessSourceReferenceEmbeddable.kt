package io.holunda.polyflow.view.jpa.process

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class ProcessSourceReferenceEmbeddable(
  @Column(name = "PROC_INSTANCE_ID", nullable = false)
  var instanceId: String,
  @Column(name = "PROC_EXECUTION_ID", nullable = false)
  var executionId: String,
  @Column(name = "PROC_DEF_ID", nullable = false)
  var definitionId: String,
  @Column(name = "PROC_DEF_KEY", nullable = false)
  var definitionKey: String,
  @Column(name = "PROC_NAME", nullable = false)
  var name: String,
  @Column(name = "APPLICATION_NAME", nullable = false)
  var applicationName: String,
  @Column(name = "TENANT_ID", nullable = true)
  var tenantId: String? = null
) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ProcessSourceReferenceEmbeddable

    if (instanceId != other.instanceId) return false
    if (executionId != other.executionId) return false
    if (definitionId != other.definitionId) return false
    if (definitionKey != other.definitionKey) return false
    if (name != other.name) return false
    if (applicationName != other.applicationName) return false
    if (tenantId != other.tenantId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = instanceId.hashCode()
    result = 31 * result + executionId.hashCode()
    result = 31 * result + definitionId.hashCode()
    result = 31 * result + definitionKey.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + applicationName.hashCode()
    result = 31 * result + (tenantId?.hashCode() ?: 0)
    return result
  }
}
