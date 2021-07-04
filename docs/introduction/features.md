---
title: Features
---

## Task pool

A task list is an application that shows a list of tasks for each individual user, based on the
user's profile, roles and authorizations. Polyflow's `taskpool` library provides a backend from which task lists
can be served. As such, it can be seen as a replacement resp. add-on for Camunda's `TaskService`. The `taskpool` library
provides the the following features:

* Task mirroring: provides a list of tasks in the system including all standard task attributes provided by Camunda BPM
* User task API providing additional attributes that are important for processing
* Reacts on all task life cycle events fired by the process engine, automatically publishes user tasks to the taskpool
* High performance queries: creates read-optimized projections including task-, process- and business data
* Centralized task list: running several Camunda BPM Engines in several applications
   is a common use case for larger companies. From the user's perspective, it is not feasible
   to login to several task lists and check for relevant user tasks. The demand for a
   centralized task list can be addressed by using the Taskpool component to which
   tasks from several process engines are transmitted over the network.

* Data enrichment: all scenarios, in which the data is not stored in the process payload, result
   in a cascade of queries executed after fetching the tasks. In contrast to that,
   the usage of the `taskpool` library with a data enrichment plugin mechanism
   (allowing to plug-in some data enricher on task creation) allows for caching  additional
   business data along with the task information.

## Data pool

Each process instance works on one or more business objects and a business object's lifecycle usually spans a longer period of time than the process instance runtime. It's a common requirement to search for these business objects (independently of process tasks) and get a list of these objects including their current statuses (e.g. DRAFT, IN_PROGRESS, COMPLETED). The `datapool` library provides the necessary features to implement a high-performance Business Object View:

* Business object API providing additional attributes important for processing
* Business object modification API for creating an audit log (aka business object history)
* Authorization API for business objects

## Process repository

A process repository provides a list of running instances and a list of process definitions deployed in the process engines connected to the library. It provides the following features:

* List of startable process definitions (including URLs to start forms)
* List of running process instances
* Reacts on life cycle events of process instance

## Process Variable pool

Along with business data entities being modified during the execution of the business processes, the business process instance itself holds a collection of so-called process variables, representing the state of the execution. In contrast to the business data entities, their lifecycle is bound to the lifecycle of the business process instance. For different reasons the requirement might exist to have a rapid access to the **process variables** of a running process instance, which is provided by the `taskpool` library.


## Integration

* Generic task sender
* Generic data entry sender
* Camunda BPM collector (tasks, process definitions, process instances, process variables)
