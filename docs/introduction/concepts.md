---
title: Concepts
---

Using a process engine in the orchestration layer is useful in many scenarios. The resulting application architecture depends on the scenario. This section
explains the core concepts and building blocks supported by the Polyflow libraries.

## The 10,000 feet view

The two main building blocks of the solution are **Process Application** and **Process Platform**. Sometimes you unite them inside the same deployment unit, but
we distinguish them to clarify their responsibilities.

A **Process Application** implements the solution's main business logic. It integrates a process engine that executes processes
and orchestrates business functions. During execution, users create and complete user tasks, and business data objects are modified.
The process application provides user interfaces for user tasks and business-data operations.

A **Process Platform** serves as an integration point for one or more process applications. It can integrate with a company's Single Sign-On (SSO) and
identity and access-management solutions, or be part of an intranet portal. It provides a __process-agnostic__ **task list** and **business object list**.

## Task-oriented applications

The core concept of a task-oriented solution is to model the underlying business process and to split the user interaction into parts represented by the **user
tasks**. Every user task abstracts an operation that a system user must perform. It normally includes a call to action
and fields for recording the user's decision. Examples include **Confirm Order**, **Verify Quotation**, and **Validate Document**.

User experience plays a significant role in acceptance of the overall solution. Users access each task through a dedicated UI called a **user task form**.
Each form presents only the information required to complete that task. This limitation avoids distraction and keeps the user focused on the task.

Because multiple process instances may run concurrently, a user may see several user tasks at once. A view that lists the tasks
available to a user is called a **task list**. Different task-assignment strategies can help optimise processing.

Alongside **user task forms**, which represent the work users complete, a data-oriented business-process view is a common requirement. It
focuses on the data being processed and displays the **business data entities** involved in business processes (sometimes called **workpieces**).
Depending on the application, business data entities may be created before a process runs, and their lifecycle usually extends
beyond process execution. Examples include an **Order**, **Shipment**, or **Document**. A dedicated **Business Data Form** displays the state of an
individual business data entity.

Because an application contains multiple entities, it also needs a view to search and list them: a **Business Entry List** or **Workpieces
List**. You may also need views for business data entries in a particular processing state, such as a **Current Workpiece List** or **Archive List**.
