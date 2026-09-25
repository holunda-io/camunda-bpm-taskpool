## Purpose

The Polyflow View API Client is for consumers of the task-pool and data-pool query APIs. It provides simple components for
querying configured views and defines an easy-to-use API for **callers**.

## Usage

Please put the following component to you class path:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-view-api-client</artifactId>
</dependency>
```

The components available are:

* `io.holunda.polyflow.view.DataEntryQueryClient`
* `io.holunda.polyflow.view.ProcessDefinitionQueryClient`
* `io.holunda.polyflow.view.ProcessInstanceQueryClient`
* `io.holunda.polyflow.view.ProcessVariableQueryClient`
* `io.holunda.polyflow.view.TaskQueryClient`

To initialize the client, you need to pass the `queryGateway` to it:

```kotlin
@Bean
fun myTaskClient(queryGateway: QueryGateway) = TaskQueryClient(queryGateway)

```

If you are using Kotlin, you might like the extension functions of the `QueryGateway` provided by `io.holunda.polyflow.view.QueryGatewayExt` object.  
