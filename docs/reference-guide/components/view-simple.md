---
title: In-Memory View
pageId: view-simple
---

## In-Memory View

The In-Memory View is component responsible for creating read-projections of tasks and business data entries. It implements
the Taskpool and Datapool View API and persists the projection in memory. The projection is transient and relies on event
replay on every application start. It is good for demonstration purposes if the number of events is manageable small,
but will fail to delivery high performance results on a large number of items.

### Features

* uses concurrent hash maps to store the read model
* provides single query API
* provides subscription query API (reactive)
* relies on event replay and transient token store

### Configuration options

In order to activate the in-memory implementation, please include the following dependency on your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-view-simple</artifactId>
  <version>${polyflow.version}</version>
</dependency>
```

Then, add the following annotation to any class marked as Spring Configuration
loaded during initialization:

```java
@Configuration
@EnablePolyflowSimpleView
public class MyViewConfiguration {

}
```

The view implementation provides runtime details using standard logging facility. If you
want to increase the logging level, please setup it e.g. in your `application.yaml`:

```yml
logging.level.io.holunda.polyflow.view.simple: DEBUG
```
