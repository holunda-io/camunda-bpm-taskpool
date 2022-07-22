## Core Datapool Aggregate Tuning

Data pool is implemented using DDD Aggregate pattern following the CQRS/ES style. By doing so, the event sourced aggregate
replays all events to restore its state. In case of Data Entry the aggregate is currently not storing any state, required for
command validation, so it is possible to optimize the loading process. For this purpose the following options are provided.

### Configuration properties

| Property (prefixed by `polyflow.core.data-entry`) | Description                                                                                                                                                                                        | Value  | Example | 
|---------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| `snapshot-threshold`                              | Threshold of events to create a snapshot of the dat entry aggregate                                                                                                                                | Long   | 5       |  
| `event-sourcing-repository-type`                  | The full-qualified class name of the repository. `org.axonframework.eventsourcing.EventSourcingRepository` or `io.holunda.polyflow.datapool.core.repository.FirstEventOnlyEventSourcingRepository` | String |         |

### Event Souring Repository

By default, the `EventSourcingRepository` for every Aggregate is provided by Axon Framework. This repository is supporting
loading from snapshots and will load tha last snapshot and all events occurred after the snapshot. Alternatively, you can set
the repository to `io.holunda.polyflow.datapool.core.repository.FirstEventOnlyEventSourcingRepository`. This repository loads
the first event only to restore the state. This special repository is using the first event and saves space by not creating any
snaphsots. 
