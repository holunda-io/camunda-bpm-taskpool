---
title: Usage Scenarios
---

Depending on your requirements and infrastructure available several deployment scenarios of the components is possible.

The simplest setup is to run all components on a single node. A more advanced scenario is to distribute components and connect them.

In doing so, one of the challenging issues for distribution and connecting microservices is a setup of messaging technology
supporting required message exchange patterns (MEPs) for a CQRS system. Because of different semantics of commands,
events and queries and additional requirements of event-sourced persistence a special implementation of
command bus, event bus and event store is required. In particular, two scenarios can be distinguished: using Axon Server
or using a different distribution technology.

The provided [Example application](../example-approval) is implemented several times demonstrating the following usage scenarios:

* [Single Node Scenario](single-node)
* [Distributed Scenario using Axon Server](distributed-axon-server)

It is a good idea to understand the single node scenario first and then move on to more elaborated scenarios.
