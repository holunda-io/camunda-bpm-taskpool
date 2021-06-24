---
title: Solution Architecture
---

## General Idea

The implementation of a single (small) process application can be performed mostly using the Camunda BPM library itself. If the
solution becomes larger, for example by setting up multiple engines for different processes or the load on a single process
engine becomes unmanageable for a process engine cluster, it is worth to separate the solution into __process specific__ and
__process agnostic__ parts. We call the __process specific__ part of the solution Process Application and the __process agnostic__
part - Process Platform.


Based on the assumption of the asymmetric read/write characteristics of task-oriented process applications, we decided
to apply Command Query Responsibility Segregation (CQRS) pattern during the architectural design. As a result, we supply
components to collect the user tasks from the process engines and create a read-optimized projections with user tasks
and correlated business data. The components can be easily integrated into process applications and be used as foundation
to build parts of the process platform.

## Design Decisions

We decided to build the library as a collection of loose-coupled components which can be used during the construction
of the process automation solution in different ways, depending on your link:../user-guide/scenarios[Usage Scenario].

The process platform is a central application consisting of business process independent components like a central user management,
task inbox (aka task list), archive view for business data processed, audit logs and others. One or many process applications
integrate with the process platform by implementing individual business processes and provide user tasks and business data changes to it.
They may also ship application frontends, which are integrated into/with the frontends of the process platform, including business
object views, user task forms and other required pieces.

The following diagram depicts the overall logical architecture.:

image::{{baseUrl('assets/media/process-platform-architecture.png')}}["Process Platform Architecture"]


## Implementation Decisions

The components are implemented using Kotlin programming language and rely on SpringBoot as execution environment.
They make a massive use of Axon Framework as a basis of the CQRS implementation.

image::{{baseUrl('assets/media/architecture-collector.png')}}["Process Platform Architecture"]

