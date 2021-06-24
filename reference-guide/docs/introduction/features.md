---
title: Features
---

## Supported process engines

* Camunda BPM

## Task List

A task list is an application allowing to represent a list of user tasks. This list is created based on
user's profile (including authorizations based on roles) for every user. The library provides the following features:

* user task API providing attributes important for processing
* mirroring tasks: provides a list of tasks in the system including all task attributes provided by Camunda BPM Engine
* reacts on all task life cycle events fired by the process engine
* high performance queries: creates read-optimized projections including task-, process- and business data
* centralized task list: running several Camunda BPM Engines in several applications
   is a common use case for larger companies. From the user's perspective, it is not feasible
   to login to several task lists and check for relevant user tasks. The demand for the
   centralized task list arises and can be addressed by the use of the `taskpool` library
   if the tasks from several process engines are collected and transmitted over the network.

* data enrichment: all scenarios, in which the data is not stored in the process payload, result
   in a cascade of queries executed after the task fetch. In contrast to that,
   the usage of the `taskpool` library with a data enrichment plugin mechanism
   (allowing to plug-in some data enricher on task creation) allows for caching the additional
   business data along with the task information, instead of querying it during task fetch.

## Archive List

An archive list provides a list of business objects processed during the execution of business process. Such a business
object lifecycle spans over a longer period of time than the process instance. A common requirement is to get a list
of such objects with different statuses like preliminary, in process or completed. The `datapool` library provides the
following features:

* business object API providing attributes important for processing
* business object modification API for creating an audit log
* authorization API for business objects

## Process List

A process list provides a list of running instances and a list of process definitions deployed in the process engines connected to
the library. It provides the following features:

* list of startable process definitions (including URLs to start forms)
* list of running process instances
* reacts on life cycle events of process instance
