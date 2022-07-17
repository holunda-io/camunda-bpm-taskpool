## Purpose

!!! note
    If you are looking for a convenient way to send out queries (API for **callers**), please check the [View API Client](view-api-client.md)

The Polyflow View API defines the interfaces for the **implementers** of the task-pool and the data-pool query API. It defines the main query types of the 
common read-projections. Its main purpose is to create a public stable API which is independent of the implementations. There are multiple implementations 
available:

* [In-Memory View](view-simple.md)
* [JPA View](view-jpa.md)
* [Mongo DB View](view-mongo.md)

In addition, the API supplies filtering functionality for handling requests of filtering of view results in form of attribute filters (
like `attribute=value&attrubute2=value2&task.name=taskname`). Especially, it defines the main concepts like `Criteria` and `Operator`
and generic query paging and sorting.

## Feature support matrix

#### Task API

The Task API allows to query for tasks handled by the task-pool.

| Query Type                       | Description                                                                                                                            | Payload types                   | In-Memory | JPA        | Mongo DB |
|----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|---------------------------------|-----------|------------|----------|
| TasksForUserQuery                | Retrieves a list of tasks accessible by the user (filters on username and groups only)                                                 | List<Task>                      | yes       | yes        | yes      |
| TaskForIdQuery                   | Retrieves a task by id (without any other filters)                                                                                     | Task or null                    | yes       | yes        | yes      |
| TasksForApplicationQuery         | Retrieves all tasks by given application name (without any further filters)                                                            | List<Task>                      | yes       | yes        | yes      |
| TaskWithDataEntriesForIdQuery    | Retrieves a task by id and correlates result with data entries, if available                                                           | (Task, List<DataEntry>) or null | yes       | yes        | yes      |
| TasksWithDataEntriesForUserQuery | Retrieves a list of tasks accessible by the user and applying additional filters and correlates result with data entries, if available | List<(Task, List<DataEntry>)    | yes       | incubation | yes      |
| TaskCountByApplicationQuery      | Counts tasks grouped by application names, useful for monitoring                                                                       | List<(ApplicationName, Count)>  | yes       | no         | yes      |
 

#### Process Definition API

The Process Definition API allows to query for process definitions handled by the task-pool.

| Query Type                              | Description                                                | Payload types           | In-Memory | JPA   | Mongo DB |
|-----------------------------------------|------------------------------------------------------------|-------------------------|-----------|-------|----------|
| ProcessDefinitionsStartableByUserQuery  | Retrieves a list of process definitions start-able by user | List<ProcessDefinition> | yes       | yes   | yes      |


#### Process Instance API

The Process Instance API allows to query for process instances handled by the task-pool.

| Query Type                    | Description                                                             | Payload types         | In-Memory | JPA   | Mongo DB |
|-------------------------------|-------------------------------------------------------------------------|-----------------------|-----------|-------|----------|
| ProcessInstancesByStateQuery  | Retrieves a list of process instances by state (started, finished, etc) | List<ProcessInstance> | yes       | yes   | no       |


#### Process Variable API (incubation)

The Process Variable API allows to query for process variables handled by the task-pool.

!!! warning
    The Process Variable API is supporting revision-aware queries, which are currently only supported by JPA and In-Memory implementations.  

| Query Type                       | Description                                                                                    | Payload types         | In-Memory | JPA | Mongo DB |
|----------------------------------|------------------------------------------------------------------------------------------------|-----------------------|-----------|-----|----------|
| ProcessVariablesForInstanceQuery | Retrieves a list of process variables for given process instance and matching provided filters | List<ProcessVariable> | yes       | no  | no       |


#### Data Entry API

The Data Entry API allows to query for data entries handled by the data-pool.

!!! warning
    The Data Entry API is supporting revision-aware queries, which are currently only supported by JPA and In-Memory implementations.


| Query Type                    | Description                                                                           | Payload types   | In-Memory | JPA   | Mongo DB |
|-------------------------------|---------------------------------------------------------------------------------------|-----------------|-----------|-------|----------|
| DataEntriesForUserQuery       | Retrieves a list of data entries accessible by the user with some additional filters. | List<DataEntry> | yes       | yes   | yes      |
| DataEntryForIdentityQuery     | Retrieves a a list by type and an optional id (without any other filters)             | List<DataEntry> | yes       | yes   | yes      |
| DataEntriesQuery              | Retrieves a list of data entries matching filters                                     | List<DataEntry> | yes       | yes   | yes      |


## Revision-aware query support

Projections can be built in a way, that they support and store event revision information transported by the event metadata. By doing so, you might send an
update of the model by specifying the update revision and are waiting for the eventually consistent event delivery to the projection of this update.
In order to achieve this, you might specify the minimum revision the query result must fulfill in order to match your query request. See [axon-gateway-extension](https://github.com/holixon/axon-gateway-extension)
for more details. Please note, that not all implementations are implementing this feature. Especially, Mongo DB View is currently **NOT SUPPORTING** Revision Aware queries. 


