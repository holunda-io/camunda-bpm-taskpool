package io.holunda.polyflow.view.jpa.data

import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository

interface DataEntryRepository : CrudRepository<DataEntryEntity, DataEntryId>, JpaSpecificationExecutor<DataEntryEntity> {

  /**
   * Finds all data entries with provided authorizations.
   */
  fun findAllByAuthorizedPrincipalsIn(authorizedPrincipalIds: Collection<String>): List<DataEntryEntity>

  companion object {

    /**
     * Specification for the user-defined state.
     */
    fun hasState(state: String): Specification<DataEntryEntity> =
      Specification { dataEntry, _, builder ->
        builder.equal(
          dataEntry.get<DataEntryStateEmbeddable>(DataEntryEntity::state.name).get<String>(DataEntryStateEmbeddable::state.name),
          state
        )
      }

    /**
     * Specification for the processing type.
     */
    fun hasProcessingType(processingType: ProcessingType): Specification<DataEntryEntity> =
      Specification { dataEntry, _, builder ->
        builder.equal(
          dataEntry.get<DataEntryStateEmbeddable>(DataEntryEntity::state.name).get<ProcessingType>(DataEntryStateEmbeddable::processingType.name),
          processingType.name
        )
      }

    /**
     * Specification for checking the payload attribute.
     */
    fun hasPayloadAttribute(name: String, value: String): Specification<DataEntryEntity> =
      Specification { dataEntry, _, builder ->
        val join = dataEntry.join<DataEntryEntity, Set<PayloadAttribute>>(DataEntryEntity::payloadAttributes.name)
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

    /**
     * Specification for checking authorization of multiple principals.
     */
    fun isAuthorizedFor(principals: Collection<AuthorizationPrincipal>): Specification<DataEntryEntity> =
      composeOr(principals.map { principal ->
        Specification { dataEntry, _, builder ->
          builder.isMember(
            "${principal.type}:${principal.name}",
            dataEntry.get<List<String>>(DataEntryEntity::authorizedPrincipals.name)
          )
        }
      }) ?: Specification { _, _, _ -> null }
  }
}


