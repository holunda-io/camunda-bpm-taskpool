package io.holunda.polyflow.view.jpa.task

import io.holunda.polyflow.view.jpa.data.DataEntryId
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import io.holunda.polyflow.view.jpa.process.SourceReferenceEmbeddable
import java.time.Instant
import java.time.ZoneOffset
import javax.persistence.*

/**
 * Entity representing user tasks.
 */
@Entity
@Table(name = "PLF_TASK")
class TaskEntity(
  @Id
  @Column(name = "TASK_ID", nullable = false)
  var taskId: String,
  @Column(name = "TASK_DEF_KEY", nullable = false)
  var taskDefinitionKey: String,
  @Column(name = "NAME", nullable = false)
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
  @Column(name = "BUSINESS_KEY")
  var businessKey: String? = null,
  @Column(name = "DESCRIPTION")
  var description: String? = null,
  @Column(name = "FORM_KEY")
  var formKey: String? = null,
  @Column(name = "DATE_CREATED", nullable = false)
  var createdDate: Instant = Instant.now(),
  @Column(name = "DATE_DUE")
  var dueDate: Instant? = null,
  @Column(name = "DATE_FOLLOW_UP")
  var followUpDate: Instant? = null,
  @Column(name = "OWNER_ID")
  var owner: String? = null,
  @Column(name = "ASSIGNEE_ID")
  var assignee: String? = null,
  @Column(name = "PAYLOAD")
  @Lob
  var payload: String? = null
  ) {
  override fun toString() = "Task[taskId=$taskId, taskDefinitionKey=$taskDefinitionKey, name=$name, created=${createdDate.atOffset(ZoneOffset.UTC)}]"
}
