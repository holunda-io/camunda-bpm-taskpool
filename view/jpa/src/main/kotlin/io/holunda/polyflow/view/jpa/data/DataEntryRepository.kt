package io.holunda.polyflow.view.jpa.data

import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository

interface DataEntryRepository : CrudRepository<DataEntryEntity, DataEntryId>, JpaSpecificationExecutor<DataEntryEntity> {

  fun findAllByAuthorizedPrincipalsIn(authorizedPrincipals: Set<out AuthorizationPrincipal>): List<DataEntryEntity>

  companion object {
    fun hasState(state: String): Specification<DataEntryEntity> =
      Specification { dataEntry, _, builder -> builder.equal(dataEntry.get<DataEntryStateEmbeddable>("state").get<String>("state"), state) }

    fun hasProcessingType(processingType: ProcessingType): Specification<DataEntryEntity> =
      Specification { dataEntry, _, builder -> builder.equal(dataEntry.get<DataEntryStateEmbeddable>("state").get<ProcessingType>("processingType"), processingType) }

  }
}


