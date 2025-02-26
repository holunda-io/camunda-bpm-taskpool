package io.holunda.camunda.taskpool.upcast.task

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.upcast.AnnotatedEventUpcaster
import io.holunda.camunda.taskpool.upcast.AnnotationBasedSingleEventUpcaster
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.dom4j.Document

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskCreatedEngineEvent3To4Upcaster.RESULT_OBJECT_TYPE, "3")
class TaskCreatedEngineEvent3To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskCompletedEngineEvent3To4Upcaster.RESULT_OBJECT_TYPE, "3")
class TaskCompletedEngineEvent3To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskCompletedEngineEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskDeletedEngineEvent3To4Upcaster.RESULT_OBJECT_TYPE, "3")
class TaskDeletedEngineEvent3To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskDeletedEngineEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskAssignedEngineEvent3To4Upcaster.RESULT_OBJECT_TYPE, "3")
class TaskAssignedEngineEvent3To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskAssignedEngineEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}


/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskAttributeUpdatedEngineEvent3To4Upcaster.RESULT_OBJECT_TYPE, "3")
class TaskAttributeUpdatedEngineEvent3To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskAttributeUpdatedEngineEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskCandidateGroupChanged1To4Upcaster.RESULT_OBJECT_TYPE, "1")
class TaskCandidateGroupChanged1To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskCandidateGroupChanged"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskCandidateUserChanged1To4Upcaster.RESULT_OBJECT_TYPE, "1")
class TaskCandidateUserChanged1To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskCandidateUserChanged"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskClaimedEvent2To4Upcaster.RESULT_OBJECT_TYPE, "2")
class TaskClaimedEvent2To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskClaimedEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskUnclaimedEvent2To4Upcaster.RESULT_OBJECT_TYPE, "2")
class TaskUnclaimedEvent2To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskUnclaimedEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskToBeCompletedEvent2To4Upcaster.RESULT_OBJECT_TYPE, "2")
class TaskToBeCompletedEvent2To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskToBeCompletedEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskDeferredEvent2To4Upcaster.RESULT_OBJECT_TYPE, "2")
class TaskDeferredEvent2To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskDeferredEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

/**
 * Upcaster fixing source reference changed introduced by #242.
 */
@AnnotatedEventUpcaster(TaskUndeferredEvent2To4Upcaster.RESULT_OBJECT_TYPE, "2")
class TaskUndeferredEvent2To4Upcaster : AbstractSourceReferenceElementRemovingUpcaster() {
  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskUndeferredEvent"
  }

  override fun getType(): String = RESULT_OBJECT_TYPE
}

private val logger = KotlinLogging.logger {}

/**
 * Abstract upcaster to be used for all task events. Removes the element duplicates from the source-reference tag introduced by the
 * usage of a sealed class instead of the interface.
 */
abstract class AbstractSourceReferenceElementRemovingUpcaster : AnnotationBasedSingleEventUpcaster() {

  companion object {
    val TAG_NAMES = arrayOf("instanceId", "executionId", "definitionId", "definitionKey", "name", "applicationName")
  }

  init {
    logger.debug { "EVENT-UPCASTER-001: Activating ${this::class.simpleName} for ${getType()}" }
  }


  override fun doUpcast(representation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    return representation.upcastPayload(
      SimpleSerializedType(getType(), "4"),
      Document::class.java
    ) { document ->
      document.apply {
        TAG_NAMES.forEach {
          removeWrongElement(document, it)
        }
      }
    }
  }

  /**
   * Retrieves the type of resulting object.
   */
  abstract fun getType(): String

  private fun removeWrongElement(document: Document, tagName: String) {
    val nodes = document.selectNodes("//${tagName}[@defined-in='io.holunda.camunda.taskpool.api.task.SourceReference']")
    nodes.forEach {
      it.parent.remove(it)
    }
  }
}
