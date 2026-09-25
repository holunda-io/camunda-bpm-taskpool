---
title: Usage Scenarios
---

Several component deployment scenarios are possible, depending on your requirements and available infrastructure.

The simplest setup is to run all components on a single node. A more advanced scenario is to distribute components and connect them.

One challenge in distributing and connecting microservices is setting up messaging technology that supports the required message-exchange
patterns (MEPs) for a CQRS system. Because commands, events, and queries have different semantics, and event-sourced persistence has additional requirements,
the command bus, event bus, and event store need specialised implementations. In particular, two scenarios are possible: using Axon Server
or another distribution technology.

The provided [example application](../example-approval.md) is implemented several times to demonstrate the following usage scenarios:

* [Single Node Scenario](single-node.md)
* [Distributed Scenario using Axon Server](distributed-axon-server.md)
* [Distributed Scenario using Axon Server with Local Polyflow Core](distributed-axon-server-local.md)

Start with the single-node scenario before moving on to more advanced scenarios.
