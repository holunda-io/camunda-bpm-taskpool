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
   * Tracking mode, defaults to event handler.
   */
  val changeTrackingMode: ChangeTrackingMode = ChangeTrackingMode.EVENT_HANDLER,
  /**
   * Configuration of the change stream.
   */
  @NestedConfigurationProperty
  val changeStream: ChangeStream = ChangeStream(),
  /**
   * Configuration for the indexes.
   */
  @NestedConfigurationProperty
  val indexes: Indexes = Indexes(),

  /**
   * Flag indicating if the data entry deletion event should delete elements.
   */
  val deleteDeletedDataEntries: Boolean = true
)

/**
 * Configures the change stream-based change tracking. Only relevant if [TaskPoolMongoViewProperties.changeTrackingMode] is `CHANGE_STREAM`.
 */
@ConstructorBinding
data class ChangeStream(
  /**
   * Configures mode to tasks deletion.
   */
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
   * While the change tracker waits for tasks that have been marked deleted to become due for clearing, it needs to buffer them. This property defines the
   * buffer capacity. If more than [bufferSize] tasks are deleted within the time window defined by [after], the buffer will overflow and the latest task(s)
   * will be dropped. These task(s) will not be automatically cleared in `CHANGE_STREAM_SUBSCRIPTION` [mode]. In `BOTH` [mode], the scheduled job will pick them
   * up and clear them eventually. Only relevant if [mode] is `CHANGE_STREAM_SUBSCRIPTION` or `BOTH`.
   */
  val bufferSize: Int = 10_000,
  /**
   * Cron expression to configure how often the job run that clears deleted tasks should run. Only relevant if [mode] is `SCHEDULED_JOB` or `BOTH`.
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
   * Subscribe to the change stream and clear any tasks that are marked deleted after the duration configured in [ChangeStream.after].
   */
  CHANGE_STREAM_SUBSCRIPTION,

  /**
   * Run a scheduled job to clear any tasks that are marked as deleted if the deletion timestamp is at least [ChangeStream.after] in the past.
   * The job is run according to the cron expression defined in [ClearDeletedTasks.jobSchedule]
   */
  SCHEDULED_JOB,

  /** Use [CHANGE_STREAM_SUBSCRIPTION] _and_ [SCHEDULED_JOB]. */
  BOTH,

  /**
   * The application is taking care of clearing deleted tasks, e.g. by implementing its own scheduled job or using a partial TTL index.
   */
  NONE
}
