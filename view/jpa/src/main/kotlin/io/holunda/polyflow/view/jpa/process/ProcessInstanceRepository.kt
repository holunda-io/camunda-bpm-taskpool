package io.holunda.polyflow.view.jpa.process

import io.holunda.polyflow.view.ProcessInstanceState
import io.holunda.polyflow.view.jpa.composeOr
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository

/**
 * Repository for process instances.
 */
interface ProcessInstanceRepository : CrudRepository<ProcessInstanceEntity, String>, JpaSpecificationExecutor<ProcessInstanceEntity> {

  companion object {
    /**
     * Checks if the instance has one of provided states.
     */
    fun hasStates(processInstanceStates: Set<ProcessInstanceState>): Specification<ProcessInstanceEntity> =
      composeOr(processInstanceStates.map { state ->
        Specification { instance, _, builder ->
          builder.equal(
            instance.get<ProcessInstanceState>(ProcessInstanceEntity::state.name),
            state
          )
        }
      }) ?: Specification { _, _, _ -> null }

  }
}
