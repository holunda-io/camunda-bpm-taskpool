---

title: Taskpool Core
pageId: core-taskpool

---
### Purpose

The component is responsible for maintaining and storing the consistent state of the taskpool
core concepts:

* Task (represents a user task instance)
* Process Definition (represents a process definition)

The component receives all commands and emits events, if changes are performed on underlying entities.
The event stream is used to store all changes (purely event-sourced) and should be used by all other
parties interested in changes.
