package io.holunda.polyflow.view.jpa.process

import io.holunda.polyflow.view.ProcessInstanceState
import jakarta.persistence.*

/**
 * Entity to store process instance.
 */
@Entity
@Table(name = "PLF_PROC_INSTANCE")
class ProcessInstanceEntity(
  @Id
  @Column(name = "INSTANCE_ID", length = 64, nullable = true)
  var processInstanceId: String,
  @Column(name = "BUSINESS_KEY", length = 64, nullable = true)
  var businessKey: String?,
  @Column(name = "SUPER_INSTANCE_ID", length = 64, nullable = true)
  var superInstanceId: String?,
  @Column(name = "START_ACTIVITY_ID", length = 64, nullable = true)
  var startActivityId: String?,
  @Column(name = "END_ACTIVITY_ID", length = 64, nullable = true)
  var endActivityId: String?,
  @Column(name = "DELETE_REASON", length = 2048, nullable = true)
  var deleteReason: String?,
  @Column(name = "START_USER_ID", length = 64, nullable = true)
  var startUserId: String?,
  @Embedded
  var sourceReference: SourceReferenceEmbeddable,
  @Column(name = "RUN_STATE", length = 64, nullable = false)
  @Enumerated(EnumType.STRING)
  var state: ProcessInstanceState
)
