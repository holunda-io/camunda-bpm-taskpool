---
title: Distributed Scenario Using Axon Server with Local Core
---

This example demonstrates Polyflow components distributed with Axon Server.
It provides two applications: the process application and the process platform.
Both are built as Spring Boot applications.

The following configuration is used in the distributed scenario with Axon Server:

* The Axon Server Connector distributes the event bus only.
* Polyflow Core components (Taskpool and Datapool) are deployed **alongside the process application**.
* Axon Server is used as the Event Store.
* PostgreSQL is used as the database for:
    - Camunda BPM Engine
    - Process Application Datasource
* JPA persists the projection view (`view-jpa`).


### System Requirements

* JDK 11
* Docker
* Docker Compose

### Preparations

Before you begin, build the entire project with `./mvnw clean install` from the project root directory.

You need backing services (Axon Server and PostgreSQL), which you can start locally
with the provided `docker-compose.yml` file.

Change to `examples/scenarios/distributed-axon-server-local-polyflow` and run the `.docker/setup.sh` preparation script.
Run it once from the command line:


```bash
cd examples/scenarios/distributed-axon-server-local-polyflow
.docker/setup.sh
```

Then start the required containers:


```bash
docker-compose up -d
```

To verify that it is running, open [http://localhost:8024/](http://localhost:8024/) in your browser. You should see
the Axon Server administration console.

### Start

The demo application consists of several Maven modules. To start the example, start these two
in the following order:

1. process-platform-view-only (process platform)
2. process-application-local-polyflow (example process application)

Start the modules with Maven from the `examples/scenarios/distributed-axon-server-local-polyflow` directory, or start the
packaged applications with:


```bash
java -jar process-platform-view-only/target/*.jar
java -jar process-application-local-polyflow/target/*.jar
```

## Useful URLs

### Process Platform
* [http://localhost:8081/polyflow/tasks](http://localhost:8081/polyflow/tasks)
* [http://localhost:8081/polyflow/archive](http://localhost:8081/polyflow/archive)
* [http://localhost:8081/swagger-ui/](http://localhost:8081/swagger-ui/)

### Process Application
* [http://localhost:8080/camunda/app/](http://localhost:8080/camunda/app/)
* [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)
