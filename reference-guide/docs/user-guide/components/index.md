---

title: Taskpool Components

---

## Components Overview

We decided to build a library as a collection of loose-coupled components which can be used during the construction
of the process automation solution. In doing so, we provide __Process Engine Components__ which are intended to be deployed as
a part of the process application. In addition, we provide __Process Platform Components__ which serve as a building blocks
for the process platform.

### Process Engine Components

Process Engine Components are designed to be a part of process application deployment
and react on engine changes / interact with the engine. These are:

* link:engine-starter[Camunda Engine Taskpool Support SpringBoot Starter]
* link:engine-interaction-client[Camunda Engine Interaction Client]
* link:engine-taskpool-collector[Taskpool Collector]
* link:engine-datapool-collector[Datapool Collector]

### Process Platform Components
Process Platform Components are designed to build a process platform.

#### Core Components
Core Components are responsible for the processing of engine commands and form an event stream
consumed by the view components. Depending on the scenario, they can be deployed either within the
process application, process platform or even completely separately.

* link:core-taskpool[Taskpool Core]
* link:core-datapool[Datapool Core]

#### View Components

View Components are responsible for creation of a unified read-only projection of tasks and business data items.
They are typically deployed as a part of the process platform.

* link:view-simple[In-Memory View]
* link:view-mongo[Mongo View]
