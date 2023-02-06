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

### Task API

The Task API allows to query for tasks handled by the task-pool.

| Query Type                        | Description                                                                                                                                    | Payload types                   | In-Memory | JPA        | Mongo DB |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------|-----------|------------|----------|
| AllTasksQuery                     | Retrieves a list of tasks applying additional filters                                                                                          | List<Task>                      | yes       | yes        | no       |
| TasksForUserQuery                 | Retrieves a list of tasks accessible by the user and applying additional filters                                                               | List<Task>                      | yes       | yes        | yes      |
| TasksForGroupQuery                | Retrieves a list of tasks accessible by the user's group and applying additional filters                                                       | List<Task>                      | yes       | yes        | no       |
| TaskForIdQuery                    | Retrieves a task by id (without any other filters)                                                                                             | Task or null                    | yes       | yes        | yes      |
| TasksForApplicationQuery          | Retrieves all tasks by given application name (without any further filters)                                                                    | List<Task>                      | yes       | yes        | yes      |
| AllTasksWithDataEntriesQuery      | Retrieves a list of tasks applying additional filters and correlates result with data entries, if available                                    | List<(Task, List<DataEntry>)    | yes       | incubation | no       |
| TasksWithDataEntriesForGroupQuery | Retrieves a list of tasks accessible by the user's group and applying additional filters and correlates result with data entries, if available | List<(Task, List<DataEntry>)    | yes       | incubation | no       |
| TasksWithDataEntriesForUserQuery  | Retrieves a list of tasks accessible by the user and applying additional filters and correlates result with data entries, if available         | List<(Task, List<DataEntry>)    | yes       | incubation | yes      |
| TaskWithDataEntriesForIdQuery     | Retrieves a task by id and correlates result with data entries, if available                                                                   | (Task, List<DataEntry>) or null | yes       | yes        | yes      |
| TaskCountByApplicationQuery       | Counts tasks grouped by application names, useful for monitoring                                                                               | List<(ApplicationName, Count)>  | yes       | no         | yes      |
 

### Process Definition API

The Process Definition API allows to query for process definitions handled by the task-pool.

| Query Type                              | Description                                                | Payload types           | In-Memory | JPA   | Mongo DB |
|-----------------------------------------|------------------------------------------------------------|-------------------------|-----------|-------|----------|
| ProcessDefinitionsStartableByUserQuery  | Retrieves a list of process definitions start-able by user | List<ProcessDefinition> | yes       | yes   | yes      |


### Process Instance API

The Process Instance API allows to query for process instances handled by the task-pool.

| Query Type                    | Description                                                             | Payload types         | In-Memory | JPA   | Mongo DB |
|-------------------------------|-------------------------------------------------------------------------|-----------------------|-----------|-------|----------|
| ProcessInstancesByStateQuery  | Retrieves a list of process instances by state (started, finished, etc) | List<ProcessInstance> | yes       | yes   | no       |


### Process Variable API (incubation)

The Process Variable API allows to query for process variables handled by the task-pool.

!!! warning
    The Process Variable API is supporting revision-aware queries, which are currently only supported by JPA and In-Memory implementations.  

| Query Type                       | Description                                                                                    | Payload types         | In-Memory | JPA | Mongo DB |
|----------------------------------|------------------------------------------------------------------------------------------------|-----------------------|-----------|-----|----------|
| ProcessVariablesForInstanceQuery | Retrieves a list of process variables for given process instance and matching provided filters | List<ProcessVariable> | yes       | no  | no       |


### Data Entry API

The Data Entry API allows to query for data entries handled by the data-pool.

!!! warning
    The Data Entry API supports revision-aware queries by JPA and In-Memory implementations **ONLY**.


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

## Filtering, Paging and Sorting

Task API and Data Entries API supports filtering, paging and sorting in queries resulting in multiple results. For Task API these are `AllTasksQuery`, `TasksForGroupQuery`, `TasksForUserQuery`,
`AllTasksWithDataEntriesQuery`, `TasksWithDataEntriesForGroupQuery`, `TasksWithDataEntriesForUserQuery` and for Data Entries API these are `DataEntriesForUserQuery` and `DataEntriesQuery`. 
The queries implement the `PageableSortableQuery` interface, allowing to limit the amount of results and provide an optional sorting:

```kotlin
interface PageableSortableQuery {
  val page: Int
  val size: Int
  val sort: String?
}
```
The `page` parameter denotes the page number to deliver (starting with `0`). The `size` parameter denotes the number of elements on a page. By default, the `page` is set to `0`
and the size is set to `Int.MAX`. 

An optional `sort` parameter allows to sort the results by a field attribute.  The format of the `sort` string is `<+|->filedName`, `+fieldName` means sort by `fieldName` ascending,
`-fieldName` means sort by `fieldName` descending. The field must be a direct member of the result (`Task`, `TaskWithDataEntries` or `DataEntry`) and must be one of the following type:

* java.lang.Integer
* java.lang.String
* java.util.Date
* java.time.Instant

To filter the results, you might supply a list of filters. A filter is an expression in format `fieldName<op>value`, where `fieldName` is addressing the attribute of the search result,
`<op>` is one of `<`, `=`, `%` and `>` and `value` is a string representation of the values. The `fieldName` can point to an attribute of the result entity itself (`Task`, `TaskWithDataEntries` 
or `DataEntry`) or point to the attribute inside the payload. To avoid a possible name clash, you must prefix the field name with `task.` if you want to filter on direct attributes of a task,
and you must prefix the field name with `data` if you want to filter on direct attributes of a dta entry. For example, `task.priority=50` would deliver tasks with priority set to 50,
and `data.entryType=info.polyflow.Order` will deliver data entries of type `info.polyflow.Order` only.

Following operations are supported:

| Filter | Operation    | In-Memory    | JPA (Task Attributes) | JPA (Data Entries Attributes)                                      | Mongo DB (Task Attributes) | Mongo DB (Data Entries Attributes) |  
|--------|--------------|--------------|-----------------------|--------------------------------------------------------------------|----------------------------|------------------------------------|
| `<`    | Less than    | all, payload | none                  | none                                                               | all, payload               | all, payload                       | 
| `>`    | Greater than | all, payload | none                  | none                                                               | all, payload               | all, payload                       |
| `=`    | Equals       | all, payload | business key, payload | entry id, entry type, type, payload, processing state, user status | all, payload               | all, payload                       |
| `%`    | Like         | all, payload | name, description     | none                                                               | none                       | none                               |


If the field name has no prefix of above, it is considered as an attribute inside the payload of data entry or enriched variables of a user task. For example, imagine
you have a data entry with payload attributes `{ "attribute": "value", "another": 45 }`. In order to search for those, just specify `attribute=value` in your filter criteria. 
