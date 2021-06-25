package io.holunda.polyflow.view.mongo.repository

import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.mongo.repository.ProcessDefinitionDocument.Companion.COLLECTION
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Mongo document representing a deployed process definition.
 */
@Document(collection = COLLECTION)
@TypeAlias("process-definition")
data class ProcessDefinitionDocument(
  @Id
  val processDefinitionId: String,
  val processDefinitionKey: String,
  val processDefinitionVersion: Int,

  val applicationName: String,
  val processName: String,
  val processVersionTag: String? = null,
  val processDescription: String? = null,
  val formKey: String? = null,
  val startableFromTasklist: Boolean = true,
  val candidateStarterUsers: Set<String> = setOf(),
  val candidateStarterGroups: Set<String> = setOf()
) {

  companion object {
    const val COLLECTION = "processes"
  }

  /**
   * Creates a projection representation out of document.
   */
  fun toProcessDefinition(): ProcessDefinition =
    ProcessDefinition(
      processDefinitionId = this.processDefinitionId,
      processDefinitionKey = this.processDefinitionKey,
      processDefinitionVersion = processDefinitionVersion,

      applicationName = this.applicationName,
      processName = this.processName,
      processVersionTag = this.processVersionTag,
      processDescription = this.processDescription,
      formKey = this.formKey,
      startableFromTasklist = this.startableFromTasklist,
      candidateStarterUsers = this.candidateStarterUsers,
      candidateStarterGroups = this.candidateStarterGroups
    )

}
