package io.holunda.camunda.taskpool.gateway.io.holunda.camunda.taskpool.gateway

/**
 * Represents revisionable model entity.
 */
interface Revisionable {
  /**
   * Revision value.
   */
  val revisionValue: RevisionValue
}
