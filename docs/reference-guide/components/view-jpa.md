---
title: JPA View
pageId: view-jpa
---
### Purpose

The JPA View is component responsible for creating read-projections of tasks and business data entries. It currently implements
Datapool View API and Taskpool API and persists the projection as entities and relations in a RDBMS using JPA. It is a useful
if the JPA persistence is already used in the project setup.

### Features

* stores representation of business data entries
* stores representation of process definitions
* stores representation of process instances
* provides single query API supporting single and subscription queries


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

```java
@Configuration
@EnablePolyflowJpaView
public class MyViewConfiguration {

}
```

In addition, configure a database connection to database using `application.properties` or `application.yaml`:

```yml
spring:
  jpa:
    show-sql: false
    open-in-view: true # disable JPA warning
  datasource:
    url: <jdbc-connnection-string>
    username: <db-user>
    password: <db-password>
```

The JPA view uses a special facility for creating search indexes on unstructured payload. For this purpose
it converts the payload into a recursive map structure (in which every primitive type is a leaf and every
complex type is decomposed via the map) using Jackson ObjectMapper and then create search indexes for all 
property paths (`myObj1.myProperty2.myOtherEmbeddedProperty3`) and their values. You can provide some 
configuration of this indexing process by the following configuration options:

```yml
polyflow.view.jpa:
  payload-attribute-level-limit: 2
  data-entry-filter:
    include: myProperty2.myOtherEmbeddedProperty3, myProperty2.myOtherEmbeddedProperty2
#    exclude: myProperty
```

In the example below you see the configuration of the limit of keying depth and usage of include/exclude filters of the keys.


The events consumed by the JPA view change data inside the database. In addition, the view sends
updates to subscription queries using the standard Axon Query Event Update Emitter mechanism. Since your
application may use transactions, you might want to configure the moment when the events are sent using the
following configuration options:
```yml
polyflow.view.jpa:
  event-emitting-type: AFTER_COMMIT # or DIRECT or BEFORE_COMMIT
```

The `DIRECT` option sends the events directly without any transaction synchronization,  
the default `AFTER_COMMIT` option sends updates after the commit and `BEFORE_COMMIT` option 
sends updates before the commit of the transaction, delivering the events.



### Logging

The view implementation provides runtime details using standard logging facility. If you
want to increase the logging level, please setup it e.g. in your `application.yaml`:

```yml
logging.level.io.holunda.polyflow.view.jpa: DEBUG
```

### DB Tables

The JPA View uses several tables to store the results. These are:

* `PLF_DATA_ENTRY`: table for business data entries
* `PLF_DATA_ENTRY_AUTHORIZATIONS`: table for authorization information of data entries
* `PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES`: table for data entry attribute search index
* `PLF_DATA_ENTRY_PROTOCOL`: table for data entry protocol entry (users, groups)
* `PLF_PROC_DEF`: table for process definitions
* `PLF_PROC_DEF_AUTHORIZATIONS`: table for authorization information of process definitions 
* `PLF_PROC_INSTANCE`: table for process instances
* `PLF_TASK`: table for user tasks
* `PLF_TASK_AUTHORIZATIONS`: table for authorization information of user tasks
* `PLF_TASK_CORRELATIONS`: table for user task correlation information
* `PLF_TASK_PAYLOAD_ATTRIBUTES`: table for user task attribute search index
* `TRACKING_TOKEN`: table for Axon Tracking Tokens

If you are interested in DDLs for the view, feel free to generate one using the following call of Apache Maven 
`mvn -Pgenerate-sql -f view/jpa`. Currently, DDLs for the databases H2, MSSQL and PostgreSQL are generated into `target/` directory.
