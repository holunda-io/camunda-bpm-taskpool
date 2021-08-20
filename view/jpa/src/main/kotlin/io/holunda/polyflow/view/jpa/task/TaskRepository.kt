package io.holunda.polyflow.view.jpa.task

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository

/**
 * Repository for accessing tasks.
 */
interface TaskRepository : CrudRepository<TaskEntity, String>, JpaSpecificationExecutor<TaskEntity> {
}
