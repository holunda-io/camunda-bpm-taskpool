---
title: Features
---

## Task Pool

A task list is an application that shows a list of tasks for each individual user, based on the user's profile, roles and authorizations. Polyflow's `taskpool`
library provides a backend from which task lists can be served.

!!! note 
    If you are using taskpool with Camunda Platform 7, it can be seen as a replacement resp. add-on for Camunda's `TaskService`.

The `taskpool` library provides the following features:

* Task mirroring: provides a list of tasks in the system including all standard task attributes provided by the process engine
* Include additional attributes that are important for processing
* Reacts on all task life cycle events fired by the process engine, automatically publishes user tasks to the `taskpool`
* High performance queries: creates read-optimized projections including task-, process- and business data
* Centralized task list: running several Camunda BPM Engines in several applications is a common use case for larger companies. From the user's perspective, it
  is not feasible to login to several task lists and check for relevant user tasks. The demand for a centralized task list can be addressed by using the
  central `taskpool` component to which tasks from several process engines are transmitted over the network.
* Data enrichment: all scenarios, in which the data is not stored in the process payload, result in a cascade of queries executed after fetching the tasks. In
  contrast to that, the usage of the `taskpool` library with a data enrichment plugin mechanism allows for caching additional business data along with the task
  information.

## Data Pool

Each process instance works on one or more business objects and a business object's lifecycle usually spans a longer period of time than the process instance
runtime. It's a common requirement to search for these business objects (independently of process tasks) and get a list of these objects including their current
statuses (e.g. DRAFT, IN_PROGRESS, COMPLETED). The `datapool` library provides the necessary features to implement a high-performance Business Object View:

* Business object API providing additional attributes important for processing
* Business object modification API for creating an audit log (aka business object history)
* Authorization API for business objects

## Process Definition Pool

A process repository provides a list of running instances and a list of process definitions deployed in the process engines connected to the library. It
provides the following features:

* List of startable process definitions (including URLs to start forms)
* List of running process instances
* Reacts on life cycle events of process instance

## Process Instance Pool

All process instances started, suspended, resumed, completed aor deleted in the process engine are reflected in the `process instance pool` component.

## Process Variable Pool

Along with business data entities being modified during the execution of the business processes, the business process instance itself holds a collection of
so-called process variables, representing the state of the execution. In contrast to the business data entities, their lifecycle is bound to the lifecycle of
the business process instance. For different reasons the requirement might exist to have rapid access to the **process variables** of a running process
instance, which is provided by the `taskpool` library.

## Integration

* Generic task sender
* Generic data entry sender
* Camunda BPM collector (tasks, process definitions, process instances, process variables)
