## Core Datapool Aggregate Tuning

Datapool uses the DDD Aggregate pattern and follows the CQRS/ES style. The event-sourced aggregate
replays events to restore its state. A Data Entry aggregate currently stores no state required for
command validation, so the loading process can be optimised. The following options are available.

### Configuration properties

| Property (prefixed by `polyflow.core.data-entry`) | Description                                                                                                                                                                                        | Value  | Example | 
|---------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| `snapshot-threshold`                              | Event threshold for creating a snapshot of the data-entry aggregate                                                                                                                                | Long   | 5       |
| `event-sourcing-repository-type`                  | The full-qualified class name of the repository. `org.axonframework.eventsourcing.EventSourcingRepository` or `io.holunda.polyflow.datapool.core.repository.FirstEventOnlyEventSourcingRepository` | String |         |
| `deletion-strategy`                               | Controls how the deletion of data entries is handled. Valid values are `lax` (default) and `strict`. See deletion strategy section below.                                                          | String | strict  |

### Event-sourcing repository

By default, Axon Framework provides an `EventSourcingRepository` for every aggregate. This repository supports
loading from snapshots and loads the latest snapshot and all events that occurred after it. Alternatively, set
the repository to `io.holunda.polyflow.datapool.core.repository.FirstEventOnlyEventSourcingRepository`. This repository loads
only the first event to restore the state and saves space by not creating snapshots.

### Deletion strategy

Data entries can be marked as deleted. When set to `strict`, no updates can be sent to a deleted data entry; otherwise, an update restores it.
