---
title: Motivation and Goals
---

Over recent years, we have built various process applications and complete process platforms for customers using Camunda Platform 7. In
doing so, we identified common requirements, particularly for task-oriented frontend applications. These requirements were
independent of the frontend technology used, and the same issues repeatedly arose during implementation.

These were:

* Addressing Camunda Platform 7 engine performance issues when displaying **large numbers** of tasks
* Creating high-performance custom queries for preloading **process variables** for tasks
* Creating high-performance custom queries to preload **business data** associated with the running process instances
* High-performance re-ordering (sorting) of user tasks
* High-performance retrieval of **tasks from multiple process engines** to display in a **single task list**
* Avoiding repetitive queries that return the same result
* Creating a custom view on the **business data items** handled during the process execution
* Creating a custom **audit log** for the changes performed on the business data items

In our projects, we developed solutions for these requirements and gained experience with
different approaches. Some of the issues above arise because data for a single user task is read
far more often than it is written. For systems with many users, this becomes a serious
performance issue that must be addressed.

A possible solution to most of these issues is a component with a read-optimised representation of user tasks. Such a component acts as a
task cache and can serve many queries without affecting process-engine performance, at the cost of strong
consistency (the task list is eventually consistent). Another component can provide business data related to process tasks.

We successfully applied this approach for multiple customers, but identified the high initial investment as its main drawback. The goal of this project
is to provide these components as free and open-source libraries that form a foundation for process platforms based on Camunda Platform 7 and other
engines. They can also be used as an integration layer for custom process applications, task lists, and other process-automation components.
