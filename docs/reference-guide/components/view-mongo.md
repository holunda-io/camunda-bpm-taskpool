---
title: Mongo View
pageId: view-mongo
---
### Purpose

The Mongo View is component responsible for creating read-projections of tasks and business data entries. It implements
the Taskpool and Datapool View API and persists the projection as document collections in a Mongo database.

### Features

* stores JSON document representation of enriched tasks, process definitions and business data entries
* provides single query API
* provides subscription query API (reactive)
* switchable subscription query API (AxonServer or MongoDB ChangeStream)


!!! warning
    Mongo DB View is currently **NOT SUPPORTING** Revision Aware queries.


### Configuration options

In order to activate the Mongo implementation, please include the following dependency on your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-view-mongo</artifactId>
  <version>${polyflow.version}</version>
</dependency>
```

The implementation relies on Spring Data Mongo and needs to activate those. Please add
the following annotation to any class marked as Spring Configuration loaded during initialization:

```java
@Configuration
@EnablePolyflowMongoView
@Import({
    org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class,
    org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration.class
  })
public class MyViewConfiguration {

}
```

In addition, configure a Mongo connection to database called `tasks-payload` using `application.properties` or
`application.yaml`:

```yml
spring:
  data:
    mongodb:
      database: tasks-payload
      host: localhost
      port: 27017
```

The view implementation provides runtime details using standard logging facility. If you
want to increase the logging level, please set up it e.g. in your `application.yaml`:


```yml
logging.level.io.holunda.polyflow.view.mongo: DEBUG
```

For further configuration, please check [Mongo DB View Configuration](../configuration/view-mongo.md)


### Collections

The Mongo View uses several collections to store the results. These are:

* data-entries: collection for business data entries
* processes: collection for process definitions
* tasks: collection for user tasks
* tracking-tokens: collection for Axon Tracking Tokens

#### Data Entries Collection

The data entries collection stores the business data entries in a uniform Datapool format.
Here is an example:

```json
{
    "_id" : "io.holunda.camunda.taskpool.example.ApprovalRequest#2db47ced-83d4-4c74-a644-44dd738935f8",
    "entryType" : "io.holunda.camunda.taskpool.example.ApprovalRequest",
    "payload" : {
        "amount" : "900.00",
        "subject" : "Advanced training",
        "currency" : "EUR",
        "id" : "2db47ced-83d4-4c74-a644-44dd738935f8",
        "applicant" : "hulk"
    },
    "correlations" : {},
    "type" : "Approval Request",
    "name" : "AR 2db47ced-83d4-4c74-a644-44dd738935f8",
    "applicationName" : "example-process-approval",
    "description" : "Advanced training",
    "state" : "Submitted",
    "statusType" : "IN_PROGRESS",
    "authorizedUsers" : [
        "gonzo",
        "hulk"
    ],
    "authorizedGroups" : [],
    "protocol" : [
        {
            "time" : ISODate("2019-08-21T09:12:54.779Z"),
            "statusType" : "PRELIMINARY",
            "state" : "Draft",
            "username" : "gonzo",
            "logMessage" : "Draft created.",
            "logDetails" : "Request draft on behalf of hulk created."
        },
        {
            "time" : ISODate("2019-08-21T09:12:55.060Z"),
            "statusType" : "IN_PROGRESS",
            "state" : "Submitted",
            "username" : "gonzo",
            "logMessage" : "New approval request submitted."
        }
    ]
}
```

#### Tasks Collections

Tasks are stored in the following format (an example):

```json
{
    "_id" : "dc1abe54-c3f3-11e9-86e8-4ab58cfe8f17",
    "sourceReference" : {
        "_id" : "dc173bca-c3f3-11e9-86e8-4ab58cfe8f17",
        "executionId" : "dc1a9742-c3f3-11e9-86e8-4ab58cfe8f17",
        "definitionId" : "process_approve_request:1:91f2ff26-a64b-11e9-b117-3e6d125b91e2",
        "definitionKey" : "process_approve_request",
        "name" : "Request Approval",
        "applicationName" : "example-process-approval",
        "_class" : "process"
    },
    "taskDefinitionKey" : "user_approve_request",
    "payload" : {
        "request" : "2db47ced-83d4-4c74-a644-44dd738935f8",
        "originator" : "gonzo"
    },
    "correlations" : {
        "io:holunda:camunda:taskpool:example:ApprovalRequest" : "2db47ced-83d4-4c74-a644-44dd738935f8",
        "io:holunda:camunda:taskpool:example:User" : "gonzo"
    },
    "dataEntriesRefs" : [
        "io.holunda.camunda.taskpool.example.ApprovalRequest#2db47ced-83d4-4c74-a644-44dd738935f8",
        "io.holunda.camunda.taskpool.example.User#gonzo"
    ],
    "businessKey" : "2db47ced-83d4-4c74-a644-44dd738935f8",
    "name" : "Approve Request",
    "description" : "Please approve request 2db47ced-83d4-4c74-a644-44dd738935f8 from gonzo on behalf of hulk.",
    "formKey" : "approve-request",
    "priority" : 23,
    "createTime" : ISODate("2019-08-21T09:12:54.872Z"),
    "candidateUsers" : [
        "fozzy",
        "gonzo"
    ],
    "candidateGroups" : [],
    "dueDate" : ISODate("2019-06-26T07:55:00.000Z"),
    "followUpDate" : ISODate("2023-06-26T07:55:00.000Z"),
    "deleted" : false
}
```

#### Process Collection

Process definition collection allows for storage of startable process definitions, deployed in a Camunda Engine.
This information is in particular interesting, if you are building a process-starter component and want to react
dynamically on processes deployed in your landscape.


```json
{
    "_id" : "process_approve_request:1:91f2ff26-a64b-11e9-b117-3e6d125b91e2",
    "processDefinitionKey" : "process_approve_request",
    "processDefinitionVersion" : 1,
    "applicationName" : "example-process-approval",
    "processName" : "Request Approval",
    "processDescription" : "This is a wonderful process.",
    "formKey" : "start-approval",
    "startableFromTasklist" : true,
    "candidateStarterUsers" : [],
    "candidateStarterGroups" : [
        "muppetshow",
        "avengers"
    ]
}
```


#### Tracking Token Collection

The Axon Tracking Token reflects the index of the event processed by the Mongo View and is stored in the
following format:


```json
{
    "_id" : ObjectId("5d2b45d6a9ca33042abea23b"),
    "processorName" : "io.holunda.camunda.taskpool.view.mongo.service",
    "segment" : 0,
    "owner" : "18524@blackstar",
    "timestamp" : NumberLong(1566379093564),
    "token" : { "$binary" : "PG9yZy5heG9uZnJhbWV3b3JrLmV2ZW50aGFuZGxpbmcuR2xvYmFsU2VxdWVuY2VUcmFja2luZ1Rva2VuPjxnbG9iYWxJbmRleD40NDwvZ2xvYmFsSW5kZXg+PC9vcmcuYXhvbmZyYW1ld29yay5ldmVudGhhbmRsaW5nLkdsb2JhbFNlcXVlbmNlVHJhY2tpbmdUb2tlbj4=", "$type" : "00" },
    "tokenType" : "org.axonframework.eventhandling.GlobalSequenceTrackingToken"
}
```
