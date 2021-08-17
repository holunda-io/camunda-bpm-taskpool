---
title: Distributed Scenario using Axon Server
---

This example is demonstrating the usage of the Camunda BPM Taskpool with components distributed with help of Axon Server.
It provides two applications for demonstration purposes: the process application and the process platform. Both applications are built as SpringBoot applications.

The following configuration is used in the distributed scenario with Axon Server:

* Bus distribution is provided by Axon Server Connector (command bus, event bus, query bus)
* Axon Server is used as Event Store
* Postgresql is used as a database for:
    - Camunda BPM Engine
    - Process Application Datasource
* Mongo is used as persistence for projection view (`mongo-view`)


### System Requirements

* JDK 11
* Docker
* Docker Compose

### Preparations

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

To verify it is running, open your browser [http://localhost:8024/](http://localhost:8024/). You should see
the Axon Server administration console.

### Start

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

## Useful URLs

### Process Platform
* [http://localhost:8081/taskpool/](http://localhost:8081/polyflow/)
* [http://localhost:8081/swagger-ui/](http://localhost:8081/swagger-ui/)

### Process Application
* [http://localhost:8080/camunda/app/](http://localhost:8080/camunda/app/)
* [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)
