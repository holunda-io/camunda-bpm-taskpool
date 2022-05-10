---
title: Scenario for running on a single node
---

This example demonstrates the usage of the Camunda BPM Taskpool deployed in one single node and is built as a SpringBoot application 
described in the [Deployment](../../introduction/deployment.md) section.

### System Requirements

* JDK 11

### Preparations

Before you begin, please build the entire project with `./mvnw clean install` from the command line in the project root directory.

### Start

The demo application consists of one Maven module which can be started by running from command line in
the `examples/scenarios/single-node` directory using Maven. Alternatively you can start the packaged application using:

```bash
java -jar target/*.jar
```

## Useful URLs

* [http://localhost:8080/taskpool/](http://localhost:8080/polyflow/)
* [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)
* [http://localhost:8080/camunda/app/tasklist/default/](http://localhost:8080/camunda/app/tasklist/default/)
