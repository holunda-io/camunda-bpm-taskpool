---
title: Taskpool Components
---

We decided to build a library as a collection of loose-coupled components which can be used during the construction
of the process automation solution. In doing so, we provide __Process Engine Integration Components__ which are intended to be deployed as
a part of the process application. In addition, we provide __Process Platform Components__ which serve as a building blocks
for the process platform itself.

## Process Engine Integration Components

The Process Engine Integration Components are designed to be a part of process application deployment
and react on engine changes / interact with the engine. These are split into common components which are
independent of the used product and framework-dependent adapters:

### Common Integration Components

* [Datapool Sender](common-datapool-sender/)
* [Taskpool Sender](common-taskpool-sender/)

### Camunda Integration Components

* [Camunda BPM Engine Interaction Client](camunda-interaction-client/)
* [Camunda BPM Engine Taskpool Collector](camunda-taskpool-collector/)
* [Camunda BPM Engine Taskpool Spring Boot Starter](camunda-starter/)

## Process Platform Components
Process Platform Components are designed to build the process platform. The platform itself provides common functionality independent used
by all process applications like common user management, single task list and so on.

### Core Components
Core Components are responsible for the processing of engine commands and form an event stream
consumed by the view components. Depending on the scenario, they can be deployed either within the
process application, process platform or even completely separately.

* [Taskpool Core](core-taskpool/)
* [Datapool Core](core-datapool/)

### View Components

View Components are responsible for creation of a unified read-only projection of tasks and business data items.
They are typically deployed as a part of the process platform.

* [In-Memory View](view-simple/)
* [Mongo DB View](view-mongo/)

## Other Components

* [Variable Serializer](other-variable-serilizer/)
* [Property Form URL Resolver](other-form-url-resolver/)
* [Tasklist URL Resolver](other-tasklist-url-resolver/)
