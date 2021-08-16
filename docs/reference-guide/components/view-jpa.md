---
title: JPA View
pageId: view-jpa
---

## JPA View

### Purpose

The JPA View is component responsible for creating read-projections of tasks and business data entries. It currently implements
Datapool View API (Taskpool API will follow) and persists the projection as entities and relations in a RDBMS using JPA. It is a useful
if the JPA persistence is already used in the project setup.

### Features

* stores representation of business data entries
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
In addition, configure a JPA connection to database using `application.properties` or `application.yaml`:

```yml
spring:
  data:
    jpa:
```

The JPA view uses a special facility for creating search indexes on unstructured payload. You can provide some
configuration of this indexing process by the following configuration options:

```yml
polyflow.view.jpa:
  payload-attribute-level-limit: 2
```

### Logging

The view implementation provides runtime details using standard logging facility. If you
want to increase the logging level, please setup it e.g. in your `application.yaml`:

```yml
logging.level.io.holunda.polyflow.view.jpa: DEBUG
```


### Tables

The JPA View uses several tables to store the results. These are:

* PLF_DATA_ENTRY: table for business data entries
* PLF_DATA_ENTRY_AUTHORIZATIONS: table for authorization information of data entries
* PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES: table for data entry protocol entry (users, groups)
* PLF_DATA_ENTRY_PROTOCOL: table for data entry protocol entry (users, groups)
* TRACKING_TOKEN: table for Axon Tracking Tokens

IF you are interested in DDLs for the view, feel free to generate one using the following call of Apache Maven `mvn clean test -DskipTests -Pgenerate-sql -f view/jpa`.
Currently, DDLs for the databases H2, MSSQL and PostgreSQL are generated.
