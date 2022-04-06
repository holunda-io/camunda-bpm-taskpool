package io.holunda.polyflow.view.mongo.task

import io.holunda.polyflow.view.mongo.data.DataEntryDocument.Companion.authorizedPrincipals
import java.time.Instant

class TaskDocumentBuilder {
  private var id = "task:1"
  private var candidateUsers = setOf<String>()
  private var candidateGroups = setOf<String>()
  private var createTime = Instant.EPOCH
  private var businessKey = "business-key"
  private var priority = 80

  private val processInstanceId = "process:1"
  private val executionId = processInstanceId
  private val processDefinitionId = "process:definition:1"
  private val processDefinitionKey = "process.definition.1"
  private val processDefinitionName = "Process Definition 1"
  private val applicationName = "Application 1"
  private val taskDefinitionKey = "task1"
  private val assignee: String? = null
  private val owner = assignee
  private val dueDate = Instant.EPOCH
  private val followUpDate = Instant.EPOCH
  private var deleted = false
  private var deleteTime: Instant? = null

  fun id(id: String): TaskDocumentBuilder {
    this.id = id
    return this
  }

  fun candidateUsers(candidateUsers: Set<String>): TaskDocumentBuilder {
    this.candidateUsers = candidateUsers
    return this
  }

  fun candidateGroups(candidateGroups: Set<String>): TaskDocumentBuilder {
    this.candidateGroups = candidateGroups
    return this
  }

  fun createTime(createTime: Instant): TaskDocumentBuilder {
    this.createTime = createTime
    return this
  }

  fun businessKey(businessKey: String): TaskDocumentBuilder {
    this.businessKey = businessKey
    return this
  }

  fun priority(priority: Int): TaskDocumentBuilder {
    this.priority = priority
    return this
  }

  fun deleted(): TaskDocumentBuilder {
    this.deleted = true
    return this
  }

  fun deleteTime(deleteTime: Instant?): TaskDocumentBuilder {
    this.deleteTime = deleteTime
    return this
  }

  fun build(): TaskDocument {
    return TaskDocument(
      id,
      ProcessReferenceDocument(
        processInstanceId,
        executionId,
        processDefinitionId,
        processDefinitionKey,
        processDefinitionName,
        applicationName,
        null
      ),
      taskDefinitionKey,
      mutableMapOf(),
      mutableMapOf(),
      java.util.Set.of(),
      businessKey,
      "$id Name",
      id + "Description",
      "$id Form Key",
      priority,
      createTime,
      candidateUsers,
      candidateGroups,
      authorizedPrincipals(candidateUsers, candidateGroups),
      assignee,
      owner,
      dueDate,
      followUpDate,
      deleted,
      deleteTime
    )
  }
}
