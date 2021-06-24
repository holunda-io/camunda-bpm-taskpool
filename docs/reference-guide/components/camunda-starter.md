---
title: Camunda Engine Taskpool Support SpringBoot Starter
pageId: engine-starter
---

## Camunda Engine Taskpool Support SpringBoot Starter

### Purpose

The Camunda Engine Taskpool Support SpringBoot Starter is a convenience module providing a single
module dependency to be included in the process application. It includes all process application
modules and provides meaningful defaults for their options.

### Configuration

In order to enable the starter, please put the following annotation on any `@Configuration` annotated
class of your SpringBoot application.


```java
@SpringBootApplication
@EnableProcessApplication
@EnableTaskpoolEngineSupport <1>
public class MyApplication {

  public static void main(String... args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```
<1> Annotation to enable the engine support.

The `@EnableTaskpoolEngineSupport` annotation has the same effect as the following block of annotations:


```java
@EnableCamundaSpringEventing
@EnableCamundaEngineClient
@EnableTaskCollector
@EnableDataEntryCollector
public class MyApplication {
  //...
}
```
