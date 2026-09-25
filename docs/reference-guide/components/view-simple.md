---
title: In-Memory View
pageId: view-simple
---

## Purpose

The In-Memory View is a component responsible for creating read projections of tasks and business data entries. It implements
the Taskpool and Datapool View API and persists the projection in memory. The projection is transient and relies on event
replay on every application start. It is suitable for demonstrations when the number of events is manageable,
but does not deliver high performance for large numbers of items.

### Features

* uses concurrent hash maps to store the read model
* provides single query API
* provides subscription query API (reactive)
* relies on event replay and transient token store

### Configuration options

To activate the in-memory implementation, include the following dependency on your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-view-simple</artifactId>
  <version>${polyflow.version}</version>
</dependency>
```

Then, add the following annotation to any class marked as Spring Configuration
that is loaded during initialisation:

```java
@Configuration
@EnablePolyflowSimpleView
public class MyViewConfiguration {

}
```

The view implementation provides runtime details through standard logging. To
increase the logging level, configure it in `application.yaml`:

```yml
logging.level.io.holunda.polyflow.view.simple: DEBUG
```
