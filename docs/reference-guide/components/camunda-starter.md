---
title: Camunda Engine Taskpool Support Spring Boot Starter
pageId: engine-starter
---

### Purpose

The Polyflow Camunda Platform 7 Spring Boot Starter is a convenience module that provides a single
dependency for a process application. It includes all process-application
modules and provides meaningful defaults for their options.

### Configuration

To enable the starter, add the following dependency to your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-camunda-bpm-springboot-starter</artifactId>
</dependency>
```

The included `TaskpoolEngineSupportConfiguration` is a Spring Boot auto-configuration that configures the required components.
To configure it manually, add the `@EnableTaskpoolEngineSupport` annotation to any `@Configuration`-annotated
class in your Spring Boot application.

The `@EnableTaskpoolEngineSupport` annotation has the same effect as the following block of annotations:

```java
@EnableCamundaTaskpoolCollector
@EnableDataEntrySender
public class MyApplication {
  //...
}
```
