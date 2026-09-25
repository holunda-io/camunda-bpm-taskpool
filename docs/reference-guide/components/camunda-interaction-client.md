---
title: Camunda Engine Interaction Client
pageId: engine-interaction-client
---

## Camunda Engine Interaction Client


### Purpose

This component applies changes delivered by Camunda Interaction Events to the Camunda BPM engine.
The following Camunda Interaction Events are supported:

* Claim User Task
* Unclaim User Task
* Defer User Task
* Undefer User Task
* Complete User Task

### Usage and configuration

To use Camunda Engine Interaction Client, add the following artifact to your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-camunda-bpm-engine-client</artifactId>
</dependency>
```

In `application.yml`, configure the process-engine application name that receives commands:

```yml
polyflow:
  integration:
    client:
      camunda:
        application-name: my-process-application # defaults to ${spring.application.name}
```
