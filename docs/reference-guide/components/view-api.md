### Purpose

The Polyflow View API defines the interfaces of the task-pool and data-pool query API. It defines the main queries of the common read-projections. Its main
purpose is to create a public stable API which is independent of the implementations. There are multiple implementations available:

* [In-Memory View](view-simple.md)
* [JPA View](view-jpa.md)
* [Mongo DB View](view-mongo.md)

In addition, the API supplies filtering functionality for handling requests of filtering of view results in form of attribute filters (
like `attribute=value&attrubute2=value2&task.name=taskname`). Especially it defines the main concepts like `Criteria` and `Operator`
and allows classification of those. 

