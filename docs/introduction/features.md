---
title: Features
---

## Task Pool

A task list is an application that shows a list of tasks for each individual user, based on the user's profile, roles and authorizations. Polyflow's `taskpool`
library provides a backend from which task lists can be served.

!!! note 
    When you use Taskpool with Camunda Platform 7, it can serve as a replacement for or extension of Camunda's `TaskService`.

The `taskpool` library provides the following features:

* Task mirroring: provides a list of tasks in the system including all standard task attributes provided by the process engine
* Includes additional attributes that are important for processing.
* Reacts to all task lifecycle events fired by the process engine and automatically publishes user tasks to the `taskpool`.
* Provides high-performance queries through read-optimised projections of task, process, and business data.
* Centralized task list: running several Camunda BPM Engines in several applications is a common use case for larger companies. From the user's perspective, it
  is not feasible to login to several task lists and check for relevant user tasks. The demand for a centralized task list can be addressed by using the
  central `taskpool` component to which tasks from several process engines are transmitted over the network.
* Data enrichment: when data is not stored in the process payload, fetching tasks can trigger a cascade of queries. The `taskpool` library's data-enrichment
  plugin mechanism can cache additional business data alongside task information.

## Data Pool

Each process instance works on one or more business objects and a business object's lifecycle usually spans a longer period of time than the process instance
runtime. It's a common requirement to search for these business objects (independently of process tasks) and get a list of these objects including their current
statuses (e.g. DRAFT, IN_PROGRESS, COMPLETED). The `datapool` library provides the necessary features to implement a high-performance Business Object View:

* A business-object API that provides additional processing attributes.
* A business-object modification API for creating an audit log (business-object history).
* An authorisation API for business objects.

## Process Definition Pool

A process repository provides a list of running instances and a list of process definitions deployed in the process engines connected to the library. It
provides the following features:

* List of startable process definitions (including URLs to start forms)
* List of running process instances
* Reacts on life cycle events of process instance

## Process Instance Pool

All process instances that are started, suspended, resumed, completed, or deleted in the process engine are reflected in the `process instance pool` component.

## Process Variable Pool

Along with business data entities being modified during the execution of the business processes, the business process instance itself holds a collection of
so-called process variables, representing the state of the execution. In contrast to the business data entities, their lifecycle is bound to the lifecycle of
the business process instance. For different reasons the requirement might exist to have rapid access to the **process variables** of a running process
instance, which is provided by the `taskpool` library.

## Integration

* Generic task sender
* Generic data entry sender
* Camunda BPM collector (tasks, process definitions, process instances, process variables)
