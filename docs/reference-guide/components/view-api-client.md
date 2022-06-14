## Purpose

The Polyflow View API Client is a client for the users of the task-pool and the data-pool query API. It provides simple components which can be used 
in order to query the configured views.  

## Usage

Pleas put the following component to you class path:

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

If you are using Kotlin, you might like the extension functions of the `QueryGateway` provided by `io.holunda.polyflow.view.QueryGatewayExt` object.  
