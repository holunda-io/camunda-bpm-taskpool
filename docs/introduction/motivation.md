---
title: Motivation
---

## Motivation and Goal

Over last years, we built variuos process applications and whole platforms our customers using the Camunda BPM engine.
In doing so, we were able to extract some common requirements, in particular with respect to task-oriented frontend applications.
These were basic requirements independent of the used frontend technology and it turned out that some issues occurred
every time during the implementation.

These were:

* Coping with performance issues of Camunda BPM engine when it comes to big amounts of tasks to be shown (total tasks, tasks per user)
* Creating high-performance custom queries for pre-loading process variables for tasks
* Creating high-performance custom queries to pre-load business data associated with the running process instance
* High-performance re-ordering (sorting) of user tasks
* High-performance retrieval of tasks from several process engines
* Repetitive queries with same result
* Creating a custom view on the business data items handled during the process execution
* Creating a a customized audit log for the changes performed on the business data items

In our projects we developed solutions to those requirements and gathered experience in applying
different approaches for that. Some issues listed above result from the fact that data on a single user task is being read
much more often than written, depending on the user count. For systems with a big amount of users this becomes a serious
performance issue and needs to be addressed.

A possible solution to most of those issues listed above is to create a special component which has a read-optimized representation of tasks.
Such a component acts as a cache for tasks and allows for serving a high amount of queries without any performance impact to the process engine
itself at the costs of loosing strong consistency (and working with eventual-consistent task list).

We successfully applied this approach at multiple customers but identified the high initial invest as a main drawback of the solution.
The goal of this project is to provide a component as a free and open source library, to be used as a foundation for creation of process platforms
for Camunda BPM. It can also be used as an integration layer for custom process applications, custom user task lists and other
components of process automation solutions.
