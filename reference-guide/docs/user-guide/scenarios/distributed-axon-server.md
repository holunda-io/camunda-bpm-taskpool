---
title: Distributed Scenario using Axon Server
---

## Distributed Scenario using Axon Server

A distributed scenario is helpful if you intend to build a central process platform and multiple process applications using it.

In general, this is the main use case for taskpool itself, but the distribution aspects adds technical complexity to the resulting
architecture. Especially, following the architecture blueprint of Axon Framework, the three buses (command bus, event bus and
query bus) needs to be distributed and act as connecting infrastructure between components.

Axon Server provides an implementation for this requirement leading to a distributed buses and a central event store. It is easy
to use, easy to configure and easy to run. If you need a HA setup, you will need the enterprise license of Axon Server. Essentially,
if don't have another HA ready-to use messaging, this scenario might be your way to go.

This scenario supports:

-  central process platform components (including task pool and data pool)
-  free choice for projection persistence (can be replayed)
-  no direct communication between process platform and process application is required (e.g. via REST, since it is routed via command bus)

The following configuration is used in the distributes scenario with Axon Server:

* Bus distribution is provided by Axon Server Connector (command bus, event bus, query bus)
* Axon Server is used as Event Store
* Postgresql is used as a database for:
- Camunda BPM Engine
- Process Application Datasource
* Mongo is used as persistence for projection view (`mongo-view`)

The following diagram depicts the distribution of the components and the messaging:

image::{{baseUrl('assets/media/deployment-axon-server.png')}}["Deployment of taskpool with axon server"]

### Running Example

This example is demonstrating the usage of the Camunda BPM Taskpool with components distributed with help of Axon Server.
It provides two applications for demonstration purposes: the process application and the process platform. Both applications are built as SpringBoot applications.

#### System Requirements

* JDK 8
* Docker
* Docker Compose

#### Preparations

Before you begin, please build the entire project with `mvn clean install` from the command line in the project root directory.

You will need some backing services (Axon Server, PostgreSQL, MongoDB) and you can easily start them locally
by using the provided `docker-compose.yml` file.

Before you start change the directory to `examples/scenarios/distributed-axon-server` and run a preparation script `.docker/setup.sh`.
You can do it with the following code from your command line (you need to do it once):


```bash
cd examples/scenarios/distributed-axon-server
.docker/setup.sh
```

Now, start required containers. The easiest way to do so is to run:


```bash
docker-compose up -d
```

To verify it is running, open your browser http://localhost:8024/[http://localhost:8024/]. You should see
the Axon Server administration console.

#### Start

The demo application consists of several Maven modules. In order to start the example, you will need to start only two
of them in the following order:

1. taskpool-application (process platform)
2. process-application (example process application)

The modules can be started by running from command line in the `examples/scenarios/distributed-axon-server` directory using Maven or start the
packaged application using:


```bash
java -jar taskpool-application/target/*.jar
java -jar process-application/target/*.jar
```

#### URLs

##### Process Platform
* http://localhost:8081/taskpool/[http://localhost:8081/taskpool/]
* http://localhost:8081/swagger-ui/[http://localhost:8081/swagger-ui/]

##### Process Application
* http://localhost:8080/camunda/app/[http://localhost:8080/camunda/app/]
* http://localhost:8080/swagger-ui/[http://localhost:8080/swagger-ui/]
