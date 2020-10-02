package io.holunda.camunda.taskpool.view.query

/**
 * Represents revisionable model entity.
 */
interface Revisionable {
  /**
   * Revision value.
   */
  val revisionValue: RevisionValue
}
