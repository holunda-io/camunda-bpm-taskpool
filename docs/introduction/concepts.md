---
title: Concepts
---

There are many scenarios in which the usage of a process engine as a component inside of the orchestration layer makes sence. Depending on the scenario the
resulting architecture of your application may vary. The following section explains the core concepts and building blocks of the architecture we want to support
and address by the Polyflow libraries.

## The 10,000 feet view

The two main building blocks of the solution are **Process Application** and **Process Platform**. Sometimes you unite them inside the same deployment unit, but
we differentiate them to make their responsibilities more clear.

A **Process Application** implements the main business logic of the solution. It integrate the process engine that is responsible for execution the processes.
During this execution user tasks are created and performed by the user and the business data objects are modified. For this purpose, the process application
provides user interfaces for user tasks and business data operations.

A **Process Platform** serves as an integration point of one or multiple process applications. It might integrate with a company's Single Sign-On solution and
Identity Management, be part of Intranet portal solution. It provides __process agnostic__ user **task list** and **business object list**.

## Task-oriented applications

The main idea of a task-oriented solution is to model the underlying business process and to split the user interaction into parts represented by the **user
tasks**. Every user task is an abstraction of an operation needed to be performed by the user. Usually, they include some sort of call-to-action including the
input fields to be able to input user's decision. Examples of user tasks are **
Confirm Order**, **Verify Quotation**, **Validate Document**.

User experience plays a significant role in acceptance of the overall solution. In order to access the user task a special UI, called **user task form** is
used. Every **user task form** is presenting only that limited part of the overall information to the user which is required to complete the user task. This
limitation is important in order to avoid distraction and foster focus on the user task.

Since there might be multiple process instances running concurrently, a user might see multiple user tasks in the same time. A special view listing all user
tasks available for a user is called **task list**. The application of different user task assignment strategies may be useful to get optimal processing.

Along with **user tasks forms**, representing the actual work the user has to complete, a data-oriented view on business processes is a common requirement. It
concentrates on the data being processed and display the **business data entities** involved in the business processes (sometimes called **Workpieces**).
Depending on your application, business data entities might be created before the running through business processes and usually the lifecycle of them spans
over the business process execution. Examples of business data entities are **Order**, **Shipment** or **Document**. In order to display the state of an
individual business data entity a special **Business Data Form** is designed.

And since there are multiple of them in the overall application, a special view to search and list them, a so-called **Business Entry List** or **Workpieces
List** is developed. Sometimes you are interested in business data entries in a particular processing status and develop a special view for them, for
example: **Current Workpiece List** or **Archive List**.
