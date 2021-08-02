---
title: JPA View
pageId: view-jpa
---

## JPA View

### Purpose

The JPA View is component responsible for creating read-projections of tasks and business data entries. It implements
the Taskpool and Datapool View API and persists the projection as document collections in a RDBMS using JPA. It is a useful
if the JPA persistence is used in the project already.

### Features

* stores representation of enriched tasks, process definitions and business data entries
* provides single query API


### Configuration options

In order to activate the JPA View implementation, please include the following dependency on your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-view-jpa</artifactId>
  <version>${polyflow.version}</version>
</dependency>
```

The implementation relies on Spring Data JPA and needs to activate those. 

In addition, configure a a JPA connection to database using `application.properties` or `application.yaml`:

```yml
spring:
  data:
    jpa:
```

The view implementation provides runtime details using standard logging facility. If you
want to increase the logging level, please setup it e.g. in your `application.yaml`:


```yml
logging.level.io.holunda.polyflow.view.jpa: DEBUG
```

### Tables

The JPA View uses several tables to store the results. These are:

* AUTHORIZATION_PRINCIPAL: table for authorization principals (users, groups)
* DATA_ENTRY: table for business data entries
* DATA_ENTRY_AUTHORIZATIONS: table for authorization information of data entries (relation)
* PROTOCOL_ENTRY: table for data entry protocol entry (users, groups)
* TRACKING_TOKEN: table for Axon Tracking Tokens
