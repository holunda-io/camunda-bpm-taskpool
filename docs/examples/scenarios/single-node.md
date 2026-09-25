---
title: Scenario for running on a single node
---

This example demonstrates Camunda BPM Taskpool deployed on a single node. It is a Spring Boot application
described in the [Deployment](../../introduction/deployment.md) section.

### System Requirements

* JDK 11

### Preparations

Before you begin, please build the entire project with `./mvnw clean install` from the command line in the project root directory.

### Start

The demo application consists of one Maven module. Start it with Maven from the
`examples/scenarios/single-node-jpa` directory, or start the packaged application with:

```bash
java -jar target/*.jar
```

## Useful URLs

* [http://localhost:8080/polyflow/](http://localhost:8080/polyflow/)
* [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)
* [http://localhost:8080/camunda/app/tasklist/default/](http://localhost:8080/camunda/app/tasklist/default/)
