---
title: Camunda Engine Interaction Client
pageId: engine-interaction-client
---

## Camunda Engine Interaction Client


### Purpose

This component performs changes delivered by Camunda Interaction Events on Camunda BPM engine.
The following Camunda Interaction Events are supported:

* Claim User Task
* Unclaim User Task
* Defer User Task
* Undefer User Task
* Complete User Task

### Usage and configuration

To use Camunda Engine Interaction Client please add the following artifact to your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-camunda-bpm-engine-client</artifactId>
</dependency>
```

In your `application.yml` configure the application name of your process engine, to receive commands:

```yml
polyflow:
  integration:
    client:
      camunda:
        application-name: my-process-application # defaults to ${spring.application.name}
```
