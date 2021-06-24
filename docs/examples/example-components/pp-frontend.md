---

title: Process Platform Frontend
---

The example process platform frontend provides example implementation of two views:

* Example Tasklist
* Example Workpieces List

## Example Tasklist

Example Tasklist is a simple implementation of task inbox for a single user. It provides the following features:

- Lists tasks in the system for selected user
- Allows for switching users (faking different user login for demonstration purposes)
- Tasks include information about the process, name, description, create time, due date, priority and assignment.
- Tasks include process data (from process instance)
- Tasks include correlated business data (correlated via variable from process instance)
- The list of tasks is sortable
- The list of tasks is pageable (7 items per page)
- Allows claiming / unclaiming
- Provides a deeplink to the user task form provided by the process application
- Allows starting new process instances

Here is, how it looks like showing task descriptions:

![Task list with task description](/img/example_tasklist_approve_description.png)


you can optionally show the business data correlated with user task:

![Task list with task data](/img/example_tasklist_approve_data.png)

## Example Workpieces List

The example workpieces list is provides a list of business objects / workpieces that are currently processed by
the processes even after the process has already been finished. It provides the following features:

- Lists business objects in the system for selected user
- Allows for switching users (faking different user login for demonstration purposes)
- Business objects include information about the type, status (with sub status), name, details
- Business objects include details about contained data
- Business objects include audit log with all state changes
- The list is pageable (7 items per page)
- Business object view
- Provides a deeplink to the business object view provided by the process application

Here is, how it looks like showing the audit log:

![Example Archive View](/img/example_archive_business_object.png)
