---
title: Camunda Engine Taskpool Support SpringBoot Starter
pageId: engine-starter
---

### Purpose

The Polyflow Camunda Platform 7 SpringBoot Starter is a convenience module providing a single
module dependency to be included in the process application. It includes all process application
modules and provides meaningful defaults for their options.

### Configuration

In order to enable the starter, please put the following dependency on your class path:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-camunda-bpm-springboot-starter</artifactId>
</dependency>
```

The included `TaskpoolEngineSupportConfiguration` is a SpringBoot AutoConfiguration that configures the required components.
If you want to configure it manually, please add the `@EnableTaskpoolEngineSupport` annotation on any `@Configuration` annotated
class of your SpringBoot application.

The `@EnableTaskpoolEngineSupport` annotation has the same effect as the following block of annotations:

```java
@EnableCamundaTaskpoolCollector
@EnableDataEntrySender
public class MyApplication {
  //...
}
```
