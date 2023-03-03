package io.holunda.polyflow.view.jpa.task

import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.data.DataEntryEntity
import io.holunda.polyflow.view.jpa.composeOr
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import io.holunda.polyflow.view.jpa.process.SourceReferenceEmbeddable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository

/**
 * Repository for accessing tasks.
 */
interface TaskRepository : CrudRepository<TaskEntity, String>, JpaSpecificationExecutor<TaskEntity> {

  companion object {

    /**
     * Specification for checking that the assignee is set.
     */
    fun isAssigneeSet(assigneeSet: Boolean): Specification<TaskEntity> =
      Specification { task, _, builder ->
        if (assigneeSet) {
          builder.isNotNull(task.get<String>(TaskEntity::assignee.name))
        } else {
          builder.isNull(task.get<String>(TaskEntity::assignee.name))
        }
      }

    /**
     * Is assignee set to specified user.
     */
    fun isAssignedTo(assignee: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.get<String>(TaskEntity::assignee.name),
          assignee
        )
      }
    /**
     * Specification for checking authorization of multiple principals.
     */
    fun isAuthorizedFor(principals: Collection<AuthorizationPrincipal>): Specification<TaskEntity> =
      composeOr(principals.map { principal ->
        Specification { task, _, builder ->
          builder.isMember(
            "${principal.type}:${principal.name}",
            task.get<List<String>>(TaskEntity::authorizedPrincipals.name)
          )
        }
      }) ?: Specification { _, _, _ -> null }

    /**
     * Specification for checking the application name.
     */
    fun hasApplication(application: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.get<SourceReferenceEmbeddable>(TaskEntity::sourceReference.name).get<String>(SourceReferenceEmbeddable::applicationName.name),
          application
        )
      }
    /**
     * Specification for checking the business key.
     */
    fun hasBusinessKey(businessKey: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.get<String>(TaskEntity::businessKey.name),
          businessKey
        )
      }

    /**
     * Specification for checking the name likeness.
     */
    fun likeName(pattern: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.like(
          task.get<String>(TaskEntity::name.name),
          pattern
        )
      }

    /**
     * Specification for checking the description likeness.
     */
    fun likeDescription(pattern: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.like(
          task.get<String>(TaskEntity::description.name),
          pattern
        )
      }

    /**
     * Specification for checking the description likeness.
     */
    fun likeBusinessKey(pattern: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.like(
          task.get<String>(TaskEntity::description.name),
          pattern
        )
      }


    /**
     * Specification for checking the payload attribute.
     */
    fun hasTaskPayloadAttribute(name: String, value: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        val join = task.join<DataEntryEntity, Set<PayloadAttribute>>(TaskEntity::payloadAttributes.name)
        val pathEquals = builder.equal(
          join.get<String>(PayloadAttribute::path.name),
          name
        )
        val valueEquals = builder.equal(
          join.get<String>(PayloadAttribute::value.name),
          value
        )
        builder.and(pathEquals, valueEquals)
      }
  }
}
