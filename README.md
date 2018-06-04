# Camunda BPM Taskpool
A component for pooling Camunda BPM user tasks for performance queries.

## Motivation

In the last five years, I built a custom task list for different customers about six times. Most of them were built based on Single Page Application (SPA) technologies, but some were using server-side rendered views. It turned out that some of the issues occured every time during the implementation. 

These were:

- coping with performance issues of the `TaskService` by the big amount of tasks available 
- creating performant custom queries for pre-loading process variables for tasks
- creating performant custom queries to pre-load business data associated with the process instance
- performant re-ordering (sorting) of user tasks
- performant retrieving a list of tasks from several process engines
- repetitive queries with same result

Many of those issues have to do with the fact that data on single task is written only few times, but is read hunderds of times (depending on the user count). For systems with a big amount of users this becomes a serious performance issue if not addressed.  
One of the possible solutions to most of this problems is to create a special component, which has a read-optimized representation of tasks and is pre-loads tasks from the `TaskService`. In doing so, it de-couples from the `TaskService` by the costs of loosing the consistency (and working with eventual-consistent task list), but allows for serving a high amount of queries without any performance impact to the process engine itself.

The goal of this project is to provide such component as a library, to be used in the integration layer between the Camunda BPM engine and the task list application. 

### Features

- provides a list of tasks in the system
- reaction on all task lifecycle events 
- configurable caching of tasks
- creation of read-optimized projections

### Further outlook

This library serves as a foundation of several follow-up projects and tools:

- Workload management: apart from the operative task management, the workload management is addressing issues like dynamic task assignment, optimal task distribution, skill-based task assignment, assignment based on presence etc.  For doing so, a task pool to apply all these rules dynamically is required and the `camunda-bpm-taskpool` component can be used for that.

- Centralized task list: running several Camunda BPM Engines in several applications is standard for larger companies. From the user's perspective, it is not feasible to login to several task lists and check for relevant user tasks. The demand for the centralized task-list arises and can be addressed by `camunda-bpm-taskpool` if the tasks from several process engines are collected and transmitted over the network.

- Data enrichment: all use cases in which the data is not stored in the process result in a cascade of queries executed after the task fetch. The task itself has only the information of the `executionId`, so you have to query the `RuntimeService` for the execution, load some variables from it and query external systems for further values. Another approach is presented in [https://blog.holisticon.de/2017/08/prozess-und-business-daten-hand-in-hand-mit-camunda-und-jpa/](a post from Jan Galinski), but still results in a query on the task fetch. In contrast to that, the usage of the `camunda-bpm-taskpool` with a data enrichment plugin mechanism (allowing to plug-in some data enricher on task creation) would allow for caching the additional business data along with the task information, instead of querying it during task fetch.



