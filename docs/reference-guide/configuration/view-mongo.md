## Configuration of Mongo View

The use of Mongo database or a Cosmos DB in Mongo mode as a persistence for the read projection of task-pool and data-pool may require some additional configuration, depending
on your scenario. 

!!! note
    We strongly recommend to use a clustered Mongo DB or Cosmos DB installation for persistence to avoid data loss.

The configuration of View Mongo is performed via application properties of the component, that includes the `polyflow-view-mongo`. All configuration
properties have the prefix `polyflow.view.mongo`. Here is the example with all properties:

```yaml
polyflow:
  view:
    mongo:
      change-stream:
        clear-deleted-tasks:
          after: PT1H
          buffer-size: 100000
          job-schedule: '@daily'
          job-jitter: PT1H
          job-timezone: UTC
          mode: SCHEDULED_JOB          
      change-tracking-mode: CHANGE_STREAM
      indexes:
        token-store: false
```

| Property (prefixed by `polyflow.view.mongo`)     | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | Value                              | Default                            | Example                            | 
|--------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------|------------------------------------|------------------------------------|
| `change-stream.clear-deleted-tasks.after`        | How long should we keep deleted tasks around before clearing them                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | Duration                           | Duration.ZERO                      | PT1H                               |  
| `change-stream.clear-deleted-tasks.buffer-size`  | While the change tracker waits for tasks that have been marked deleted to become due for clearing, it needs to buffer them. This property defines the buffer capacity. If more than [bufferSize] tasks are deleted within the time window defined by [after], the buffer will overflow and the latest task(s) will be dropped. These task(s) will not be automatically cleared in `CHANGE_STREAM_SUBSCRIPTION` [mode]. In `BOTH` [mode], the scheduled job will pick them up and clear them eventually. Only relevant if [mode] is `CHANGE_STREAM_SUBSCRIPTION` or `BOTH`. | Long                               | 10000                              | 200                                |
| `change-stream.clear-deleted-tasks.job-schedule` | Cron expression to configure how often the job run that clears deleted tasks should run. Only relevant if [mode] is `SCHEDULED_JOB` or `BOTH`.                                                                                                                                                                                                                                                                                                                                                                                                                             | Cron expression                    | @daily                             | @hourly                            |
| `change-stream.clear-deleted-tasks.job-jitter`   | The cleanup job execution time will randomly be delayed after what is determined by the cron expression by [0..this duration].                                                                                                                                                                                                                                                                                                                                                                                                                                             | Duration                           | PT5M                               | PT3M                               |
| `change-stream.clear-deleted-tasks.timezone`     | TimeZone to use for resolving the cron expression. Default: UTC.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | Timezone                           | UTC                                | CET                                |
| `change-stream.clear-deleted-tasks.mode`         | How exactly should we clear deleted tasks.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | see [below](#clear-deleted-tasks)  | see [below](#clear-deleted-tasks)  | see [below](#clear-deleted-tasks)  |
| `change-tracking-mode`                           | Mode to use for event tracking.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | see [below](#change-tracking-mode) | see [below](#change-tracking-mode) | see [below](#change-tracking-mode) |
| `indexes.token-store`                            | Controls the index of the token store.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | Boolean                            | true                               | false                              |


### Change Tracking Mode

The events delivering updates on view artifacts are received by the projection and cause updates in Mongo DB projections. If you are interested in delivering reactive updates (e.g. Server push, SSE, Websocket),
you can control what is the trigger for the update modification. The reason for this is that Mongo DB itself is eventually consistent in replica set and the update operation will eventually be 
written to all nodes. If you are reading the documents by the component triggered by the reactive update, you might end up in a race condition. 

Set the mode for event tracking by selecting from one of the following values:

| Value         | Description                                                                     |
|---------------|---------------------------------------------------------------------------------|
| EVENT_HANDLER | Use Axon query bus and update subscriptions after the event has been processed. |
| CHANGE_STREAM | Use Mongo DB change stream.                                                     |
| NONE          | Disable reactive updates.                                                       |


### Clear deleted tasks

Removal of elements from collection is a costly operation in distributed Mongo DB cluster. For this purpose, if the user task gets deleted, it is marked by a `deleted` flag and immediately
excluded from the selection. A later job is responsible for real wiping it out from the collection. The deletion mode controls how this operation is performed.    
Set the mode for task deletion by selecting from one of the following values: 

| Value                        | Description                                                                                                                                                                                                               |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CHANGE_STREAM_SUBSCRIPTION` | Subscribe to the change stream and clear any tasks that are marked deleted after the duration configured in `after` property.                                                                                             |
| `SCHEDULED_JOB`              | Run a scheduled job to clear any tasks that are marked as deleted if the deletion timestamp is at least `after` property in the past. The job is run according to the cron expression defined in `job-schedule` property. |
| `BOTH`                       | Use `CHANGE_STREAM_SUBSCRIPTION` _and_ `SCHEDULED_JOB`.                                                                                                                                                                   |
| `NONE`                       | The application is taking care of clearing deleted tasks, e.g. by implementing its own scheduled job or using a partial TTL index.                                                                                        |




  





