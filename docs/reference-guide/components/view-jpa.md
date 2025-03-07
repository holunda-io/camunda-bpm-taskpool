---
title: JPA View
pageId: view-jpa
---
### Purpose

The JPA View is component responsible for creating read-projections of tasks and business data entries. It currently implements
Datapool View API and Taskpool API and persists the projection as entities and relations in a RDBMS using JPA. It is useful
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
  stored-items: task, data-entry, process-instance, process-definition
  payload-attribute-level-limit: 2
  payload-attribute-column-length: 255
  include-correlated-data-entries-in-data-entry-queries: false
  process-outdated-events: false
  data-entry-filters:
    include: myProperty2.myOtherEmbeddedProperty3, myProperty2.myOtherEmbeddedProperty2
#    exclude: myProperty
  task-filters:
    exclude: processVariableWithVeryLongText

```

In the example above you see the configuration of the limit of keying depth and usage of include/exclude filters of the keys.
In addition, the `stored-items` property is holding a set of items to be persisted to the database. The possible values of 
stored items are: `task`, `data-entry`, `process-instance` and `process-definition`. By setting this property, you can disable
storage of items not required by your application and save space consumption of your database. The property defaults to `data-entry`.

With the `payload-attribute-column-length` property one can specify a maximum length for payload attribute values if they are strings. Values that exceed
this length will automatically be trimmed to the max length in order to prevent exceptions when handling the event. This is especially necessary because
relational databases have limits on the length of composite primary keys. Since the combination of (id, path, value) for tasks or (id, type, path, value) for
data entries must be unique, the primary key is very large, which limits the amount of space available for the value.

The `include-correlated-data-entries-in-data-entry-queries` flag controls whether a data entry query (`DataEntriesForUserQuery` or `DataEntriesQuery`) considers
the payload of correlated data entries. The data entry attributes (such as `entry_type`, `state.state`, ...) of correlated data entries are not considered.
*Note:* Only one level of correlation depth is considered here and there is no option yet to change the depth.

With the property `process-outdated-events` you can configure the view such that all events are processed, even when the event timestamp is older than a different event 
that was already processed. This might be helpful when doing technical updates but should be used with care as old event will override more recent changes if the 
order is not guaranteed. Defaults to `false`.

The attributes `data-entry-filters` and `task-filters` hold `include` / `exclude` lists of property paths which will be taken in 
consideration during the search index creation.


!!! note
    Please make sure you understand that the **payload enrichment** performed during collection and **indexing for search** are two different
    operations. It is perfectly fine to have a large JSON payload attached to the task, but it makes no sense to make the entire payload searchable,
    at lease using JPA View.

### Entity Scan

The JPA View utilizes Spring Data repositories and Hibernate entities inside the persistence layer. As a result, it declares a `@EntityScan` 
and `@EnableJpaRepositories` annotations pointing at the corresponding locations. If you are using Spring Data JPA on your own, you will
need to add the `@EntityScan` and `@EnableJpaRepositores` annotation pointing at your packages. In addition, please check
[Persistence configuration](../configuration/persistence.md).


### Logging

The view implementation provides runtime details using standard logging facility. If you
want to increase the logging level, please setup it e.g. in your `application.yaml`:

```yml
logging.level:
  io.holunda.polyflow.view.jpa: DEBUG
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
* `PLF_VIEW_TASK_AND_DATA_ENTRY_PAYLOAD`: view for convenient taskWithDataEntry queries execution
* `PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES`: view for convenient data entry queries with correlations
* `TRACKING_TOKEN`: table for Axon Tracking Tokens

If you are interested in DDLs for the view, feel free to generate one using the following call of Apache Maven 
`mvn -Pgenerate-sql -f view/jpa`. Currently, DDLs for the databases H2, MSSQL and PostgreSQL are generated into `target/` directory.  
The DDL for the `PLF_VIEW_TASK_AND_DATA_ENTRY_PAYLOAD` and `PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES` cannot be auto-generated, therefore you need to use the following statements to create them:
```
create view PLF_VIEW_TASK_AND_DATA_ENTRY_PAYLOAD as
((select pc.TASK_ID, dea.PATH, dea.VALUE
 from PLF_TASK_CORRELATIONS pc
          join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES dea on pc.ENTRY_ID = dea.ENTRY_ID and pc.ENTRY_TYPE = dea.ENTRY_TYPE)
union
select * from PLF_TASK_PAYLOAD_ATTRIBUTES);
```

```
create view PLF_VIEW_DATA_ENTRY_PAYLOAD as (
select *
from PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES
union
(select ec.OWNING_ENTRY_ID   as ENTRY_ID,
        ec.OWNING_ENTRY_TYPE as ENTRY_TYPE,
        ep.path              as PATH,
        ep.value             as VALUE
 from PLF_DATA_ENTRY_CORRELATIONS ec
     join PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES ep
 on
     ec.ENTRY_ID = ep.ENTRY_ID and ec.ENTRY_TYPE = ep.ENTRY_TYPE)
)
```
