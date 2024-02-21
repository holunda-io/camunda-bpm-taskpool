package io.holunda.polyflow.view.jpa.process

import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.composeOr
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository

/**
 * Spring Data JPA for Process definitions.
 */
interface ProcessDefinitionRepository : CrudRepository<ProcessDefinitionEntity, String>, JpaSpecificationExecutor<ProcessDefinitionEntity> {
  companion object {

    /**
     * Specification for checking authorization of multiple principals.
     */
    fun isStarterAuthorizedFor(principals: Collection<AuthorizationPrincipal>): Specification<ProcessDefinitionEntity> =
      composeOr(principals.map { principal ->
        Specification { processDefinition, _, builder ->
          builder.isMember(
            "${principal.type}:${principal.name}",
            processDefinition.get<List<String>>(ProcessDefinitionEntity::authorizedStarterPrincipals.name)
          )
        }
      })
  }
}
