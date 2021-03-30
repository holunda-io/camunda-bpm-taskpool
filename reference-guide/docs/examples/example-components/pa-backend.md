---

title: Process Application Backend
---

## Process Application Backend

The process application backend is implementing the process described in the link:example[Example].
It demonstrates a typical three-tier application, following the Boundary-Control-Entity pattern.

### Boundary
The REST API is defined using OpenAPI specification and is implemented by Spring MVC controllers. It defines of four
logical parts:

- Environment Controller
- Request Controller
- Approve Task Controller
- Amend Task Controller

### Control
The control tier is implemented using stateless Spring Beans and orchestrated by the Camunda BPM Process.
Typical `JavaDelegate` and `ExecutionListener` are used as a glue layer. Business services of this layer are
responsible for the integration with `Datapool Components` to reflect the status of the `Request` business entity.
The Camunda BPM Engine is configured to use the `TaskCollector` to integrate with remaining components of
the `camunda-bpm-taskpool`.

### Entity
The entity tier is implemented using Spring Data JPA, using Hibernate entities. Application data
and process engine data is stored using a RDBMS.
