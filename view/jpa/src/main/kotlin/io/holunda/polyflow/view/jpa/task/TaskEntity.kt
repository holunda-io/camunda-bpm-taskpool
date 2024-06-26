package io.holunda.polyflow.view.jpa.task

import io.holunda.polyflow.view.jpa.data.DataEntryEntity
import io.holunda.polyflow.view.jpa.data.DataEntryId
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import io.holunda.polyflow.view.jpa.process.SourceReferenceEmbeddable
import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import java.time.Instant
import java.time.ZoneOffset

/**
 * Entity representing user tasks.
 */
@Entity
@Table(name = "PLF_TASK")
class TaskEntity(
  @Id
  @Column(name = "TASK_ID", length = 64, nullable = false)
  var taskId: String,
  @Column(name = "TASK_DEF_KEY", nullable = false)
  var taskDefinitionKey: String,
  @Column(name = "NAME", length = 128, nullable = false)
  var name: String,
  @Column(name = "PRIORITY")
  var priority: Int,
  @Embedded
  var sourceReference: SourceReferenceEmbeddable,
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "PLF_TASK_AUTHORIZATIONS",
    joinColumns = [
      JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID")
    ]
  )
  @Column(name = "AUTHORIZED_PRINCIPAL", nullable = false)
  var authorizedPrincipals: MutableSet<String> = mutableSetOf(),
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "PLF_TASK_CORRELATIONS",
    joinColumns = [
      JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID")
    ]
  )
  var correlations: MutableSet<DataEntryId> = mutableSetOf(),
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "PLF_TASK_PAYLOAD_ATTRIBUTES",
    joinColumns = [
      JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID"),
    ]
  )
  var payloadAttributes: MutableSet<PayloadAttribute> = mutableSetOf(),
  @Column(name = "BUSINESS_KEY", length = 255, nullable = true)
  var businessKey: String? = null,
  @Column(name = "DESCRIPTION", length = 2048, nullable = true)
  var description: String? = null,
  @Column(name = "FORM_KEY")
  var formKey: String? = null,
  @Column(name = "DATE_CREATED", nullable = false)
  var createdDate: Instant = Instant.now(),
  @Column(name = "DATE_DUE", nullable = true)
  var dueDate: Instant? = null,
  @Column(name = "DATE_FOLLOW_UP", nullable = true)
  var followUpDate: Instant? = null,
  @Column(name = "OWNER_ID", length = 64, nullable = true)
  var owner: String? = null,
  @Column(name = "ASSIGNEE_ID", length = 64, nullable = true)
  var assignee: String? = null,
  @Column(name = "PAYLOAD")
  @Lob
  var payload: String? = null,

  @Immutable
  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name="TASK_ID", updatable = false)
  val taskAndDataEntryPayloadAttributes: Set<TaskAndDataEntryPayloadAttributeEntity>? = null,
  @Immutable
  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "PLF_TASK_CORRELATIONS",
    joinColumns = [JoinColumn(name = "TASK_ID")],
    inverseJoinColumns = [JoinColumn(name = "ENTRY_ID"), JoinColumn(name = "ENTRY_TYPE")]
  )
  val dataEntryCorrelations: Set<DataEntryEntity>? = null
) {
  override fun toString() = "Task[taskId=$taskId, taskDefinitionKey=$taskDefinitionKey, name=$name, created=${createdDate.atOffset(ZoneOffset.UTC)}]"
}
