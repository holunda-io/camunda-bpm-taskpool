package io.holunda.polyflow.view.mongo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.scheduling.support.CronExpression
import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Configures mongo projection mode.
 */
@ConfigurationProperties(prefix = "polyflow.view.mongo")
@ConstructorBinding
data class TaskPoolMongoViewProperties(
  /**
   * Tracking mode.
   */
  val changeTrackingMode: ChangeTrackingMode = ChangeTrackingMode.EVENT_HANDLER,

  @NestedConfigurationProperty
  val changeStream: ChangeStream = ChangeStream(),

  @NestedConfigurationProperty
  val indexes: Indexes = Indexes()
)

/**
 * Configures the change stream-based change tracking. Only relevant if [TaskPoolMongoViewProperties.changeTrackingMode] is `CHANGE_STREAM`.
 */
@ConstructorBinding
data class ChangeStream(
  val clearDeletedTasks: ClearDeletedTasks = ClearDeletedTasks()
)

/**
 * Configures how and when deleted tasks (as in marked as deleted) are cleared (as in gone from the database).
 */
@ConstructorBinding
data class ClearDeletedTasks(
  /** How long should we keep deleted tasks around before clearing them? Default: immediately. */
  val after: Duration = Duration.ZERO,

  /**
   * How exactly should we clear deleted tasks? See [ClearDeletedTasksMode] for an explanation of the options. Default: use a change stream subscription and a
   * job to clean up any leftovers.
   */
  val mode: ClearDeletedTasksMode = ClearDeletedTasksMode.CHANGE_STREAM_SUBSCRIPTION,

  /**
   * Cron expression to configure how often the job run that clears deleted tasks should run. Only relevant if [mode] is `SCHEDULED_JOB` or `BOTH`
   */
  val jobSchedule: String = "@daily",

  /**
   * The cleanup job execution time will randomly be delayed after what is determined by the cron expression by [0..this duration].
   */
  val jobJitter: Duration = Duration.ofMinutes(5),

  /**
   * TimeZone to use for resolving the cron expression. Default: UTC.
   */
  val jobTimezone: ZoneId = ZoneOffset.UTC
)

/**
 * Defines which indexes are created by default.
 */
@ConstructorBinding
data class Indexes(
  /**
   * Controls the index of the token store.
   */
  val tokenStore: Boolean = true
)

/**
 * Change tracking mode.
 */
enum class ChangeTrackingMode {
  /**
   * Use axon query bus and update subscriptions.
   */
  EVENT_HANDLER,

  /**
   * Use mongo change stream.
   */
  CHANGE_STREAM,

  /**
   * Disable updates.
   */
  NONE
}

/**
 * Defines mode for deletion of tasks in MongoDB.
 */
enum class ClearDeletedTasksMode {
  /**
   * Subscribe to the change stream and clear any tasks that are marked deleted after the duration configured in [ChangeStream.clearDeletedTasksAfter].
   */
  CHANGE_STREAM_SUBSCRIPTION,

  /**
   * Run a scheduled job to clear any tasks that are marked as deleted if the deletion timestamp is at least [ChangeStream.clearDeletedTasksAfter] in the past.
   * The job is run according to the cron expression defined in
   */
  SCHEDULED_JOB,

  /** Use [CHANGE_STREAM_SUBSCRIPTION] _and_ [SCHEDULED_JOB]. */
  BOTH,

  /**
   * The application is taking care of clearing deleted tasks, e.g. by implementing its own scheduled job or using a partial TTL index.
   */
  NONE
}
