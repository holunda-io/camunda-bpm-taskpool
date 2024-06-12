package io.holunda.polyflow.view.jpa.task

import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.jpa.CountByApplication
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.composeOr
import io.holunda.polyflow.view.jpa.data.DataEntryEntity
import io.holunda.polyflow.view.jpa.data.DataEntryId
import io.holunda.polyflow.view.jpa.data.DataEntryStateEmbeddable
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import io.holunda.polyflow.view.jpa.process.SourceReferenceEmbeddable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant


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
      })

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
     * Specification for checking the process name.
     */
    fun hasProcessName(processName: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.get<SourceReferenceEmbeddable>(TaskEntity::sourceReference.name).get<String>(SourceReferenceEmbeddable::name.name),
          processName
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
     * Specification for checking the assignee.
     */
    fun hasAssignee(assignee: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.get<String>(TaskEntity::assignee.name),
          assignee
        )
      }

    /**
     * Specification for checking the priority.
     */
    fun hasPriority(priority: Int): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.get<Int>(TaskEntity::priority.name),
          priority
        )
      }

    /**
     * Specification for checking the due date.
     */
    fun hasDueDate(dueDate: Instant): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.get<Instant>(TaskEntity::dueDate.name),
          dueDate
        )
      }

    /**
     * Specification for checking the due date.
     */
    fun hasDueDateBefore(dueDate: Instant): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.or(
          builder.isNull(task.get<Instant>(TaskEntity::followUpDate.name)),
          builder.lessThan(
            task.get(TaskEntity::dueDate.name),
            dueDate
          )
        )
      }

    /**
     * Specification for checking the due date.
     */
    fun hasDueDateAfter(dueDate: Instant): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.or(
          builder.isNull(task.get<Instant>(TaskEntity::followUpDate.name)),
          builder.greaterThan(
            task.get(TaskEntity::dueDate.name),
            dueDate
          )
        )
      }

    /**
     * Specification for checking the follow-up date.
     */
    fun hasFollowUpDate(followUpDate: Instant): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.get<Instant>(TaskEntity::followUpDate.name),
          followUpDate
        )
      }

    /**
     * Specification for checking the follow-up date.
     */
    fun hasFollowUpDateBefore(followUpDate: Instant): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.or(
          builder.isNull(task.get<Instant>(TaskEntity::followUpDate.name)),
          builder.lessThan(
            task.get(TaskEntity::followUpDate.name),
            followUpDate
          )
        )
      }

    /**
     * Specification for checking the follow-up date.
     */
    fun hasFollowUpDateAfter(followUpDate: Instant): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.or(
          builder.isNull(task.get<Instant>(TaskEntity::followUpDate.name)),
          builder.greaterThan(
            task.get(TaskEntity::followUpDate.name),
            followUpDate
          )
        )
      }

    /**
     * Specification for checking the name likeness.
     */
    fun likeName(pattern: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.like(
          builder.lower(
            task.get(TaskEntity::name.name)
          ),
          "%${pattern.lowercase()}%"
        )
      }

    /**
     * Specification for checking the description likeness.
     */
    fun likeDescription(pattern: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.like(
          builder.lower(
            task.get(TaskEntity::description.name)
          ),
          "%${pattern.lowercase()}%"
        )
      }

    /**
     * Specification for checking the description likeness.
     */
    fun likeBusinessKey(pattern: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.like(
          builder.lower(
            task.get(TaskEntity::businessKey.name)
          ),
          "%${pattern.lowercase()}%"
        )
      }

    /**
     * Specification for checking the process name likeness.
     */
    fun likeProcessName(pattern: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.like(
          builder.lower(
            task.get<SourceReferenceEmbeddable>(TaskEntity::sourceReference.name).get(SourceReferenceEmbeddable::name.name)
          ),
          "%${pattern.lowercase()}%"
        )
      }

    /**
     * Specification for checking likeness of multiple task attributes combined by an OR operator.
     */
    fun likeTextSearch(pattern: String): Specification<TaskEntity> =
      likeName(pattern)
        .or(likeDescription(pattern))
        .or(likeProcessName(pattern))

    /**
     * Specification for checking the payload attribute of a task. If multiple values are given, one of them must match.
     * payload.name = ? AND (payload.value = ? OR payload.value = ? OR ...)
     */
    fun hasTaskPayloadAttribute(name: String, values: List<String>): Specification<TaskEntity> =
      Specification { task, query, builder ->
        query.distinct(true)
        val join = task.join<TaskEntity, Set<PayloadAttribute>>(TaskEntity::payloadAttributes.name)
        val pathEquals = builder.equal(
          join.get<String>(PayloadAttribute::path.name),
          name
        )

        val valueAnyOf = values.map {
          builder.equal(
            join.get<String>(PayloadAttribute::value.name),
            it
          )
        }.let { builder.or(*it.toTypedArray()) }

        builder.and(pathEquals, valueAnyOf)
      }


    /**
     * Specification for checking the payload attributes of a task (including payload attributes from correlated data entries).
     * If multiple values are given, one of them must match. payload.name = ? AND (payload.value = ? OR payload.value = ? OR ...)
     */
    fun hasTaskOrDataEntryPayloadAttribute(name: String, values: List<String>): Specification<TaskEntity> =
      Specification { task, query, builder ->
        query.distinct(true)
        val join = task.join<TaskEntity, Set<TaskAndDataEntryPayloadAttributeEntity>>(TaskEntity::taskAndDataEntryPayloadAttributes.name)
        val pathEquals = builder.equal(
          join.get<TaskAndDataEntryPayloadAttributeEntityId>(TaskAndDataEntryPayloadAttributeEntity::id.name)
            .get<String>(TaskAndDataEntryPayloadAttributeEntityId::path.name),
          name
        )

        val valueAnyOf = values.map {
          builder.equal(
            join.get<TaskAndDataEntryPayloadAttributeEntityId>(TaskAndDataEntryPayloadAttributeEntity::id.name)
              .get<String>(TaskAndDataEntryPayloadAttributeEntityId::value.name),
            it
          )
        }.let { builder.or(*it.toTypedArray()) }

        builder.and(pathEquals, valueAnyOf)
      }

    /**
     * Specification for checking that a task has a correlated data entry with entry id
     */
    fun hasDataEntryEntryId(entryId: String): Specification<TaskEntity> =
      Specification { task, query, builder ->
        builder.equal(
          task.join<TaskEntity, Set<DataEntryEntity>>(TaskEntity::dataEntryCorrelations.name)
            .get<DataEntryId>(DataEntryEntity::dataEntryId.name)
            .get<String>(DataEntryId::entryId.name),
          entryId
        )
      }

    /**
     * Specification for checking that a task has a correlated data entry with entry type
     */
    fun hasDataEntryEntryType(entryType: String): Specification<TaskEntity> =
      Specification { task, query, builder ->
        builder.equal(
          task.join<TaskEntity, Set<DataEntryEntity>>(TaskEntity::dataEntryCorrelations.name)
            .get<DataEntryId>(DataEntryEntity::dataEntryId.name)
            .get<String>(DataEntryId::entryType.name),
          entryType
        )
      }

    /**
     * Specification for checking that a task has a correlated data entry with type
     */
    fun hasDataEntryType(type: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.join<TaskEntity, Set<DataEntryEntity>>(TaskEntity::dataEntryCorrelations.name)
            .get<String>(DataEntryEntity::type.name),
          type
        )
      }

    /**
     * Specification for checking that a task has a correlated data entry with entry state
     */
    fun hasDataEntryState(state: String): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.join<TaskEntity, Set<DataEntryEntity>>(TaskEntity::dataEntryCorrelations.name)
            .get<DataEntryStateEmbeddable>(DataEntryEntity::state.name)
            .get<String>(DataEntryStateEmbeddable::state.name),
          state
        )
      }

    /**
     * Specification for checking that a task has a correlated data entry with processing type
     */
    fun hasDataEntryProcessingType(processingType: ProcessingType): Specification<TaskEntity> =
      Specification { task, _, builder ->
        builder.equal(
          task.join<TaskEntity, Set<DataEntryEntity>>(TaskEntity::dataEntryCorrelations.name)
            .get<DataEntryStateEmbeddable>(DataEntryEntity::state.name)
            .get<ProcessingType>(DataEntryStateEmbeddable::processingType.name),
          processingType.name
        )
      }
  }


  /**
   * Counts user tasks grouped by application name, resulting in a total amount of tasks per application (=process engine).
   * Helpful for monitoring of tasks on the task pool projection side vs. engine side.
   * @return list of count by application groupings.
   */
  @Query("select new io.holunda.polyflow.view.jpa.CountByApplication(t.sourceReference.applicationName, count(t) ) from TaskEntity t group by t.sourceReference.applicationName")
  fun getCountByApplication(): List<CountByApplication>

}
