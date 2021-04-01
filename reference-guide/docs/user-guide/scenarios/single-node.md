---
pageId: single-node
title: Scenario for running on a single node
---

## Scenario for running on a single node

In a single node scenario, the process application and the process platform components are deployed in a single node.
In most production environments this scenario doesn't make sense because of poor reliability. Still, it is valid for
demonstration purpose and is ideal to play around with components and understand their purpose and interaction between them.

In a single mode scenario the following configuration is used:

* All buses are local (command bus, event bus, query bus)
* In-memory H2 is used as a database for:
  - Camunda BPM Engine
  - Axon Event Store (JPA-based)
  - Process Application Datasource
* In-memory transient projection view is used (`simple-view`)

Check the following diagram for more details:

image::{{baseUrl('assets/media/deployment-single.png')}}["Deployment of all component in a single node"]

### Running Example

This example demonstrates the usage of the Camunda BPM Taskpool deployed in one single node and is
built as a SpringBoot application.

#### System Requirements

* JDK 8

#### Preparations

Before you begin, please build the entire project with `./mvnw clean install` from the command line in the project root directory.

#### Start

The demo application consists of one Maven module which can be started by running from command line in
the `examples/scenarios/single-node` directory using Maven. Alternatively you can start the packaged application using:

[source,bash]
```
java -jar target/*.jar
```

#### Useful URLs

* http://localhost:8080/taskpool/[http://localhost:8080/taskpool/]
* http://localhost:8080/swagger-ui/[http://localhost:8080/swagger-ui/]
* http://localhost:8080/camunda/app/tasklist/default/[http://localhost:8080/camunda/app/tasklist/default/]
