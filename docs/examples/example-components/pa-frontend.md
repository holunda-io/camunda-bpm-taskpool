---

title: Process Application Frontend
---

The process application backend is implementing the user task forms and business object views for the
example application. It is built as a typical Angular Single Page Application (SPA) and provides
views for both user tasks and the business object. It communicates with process application backend via
REST API, defined in the latter.

The user primarily interacts with the process platform which seamless integrate with the process applications.
Usually, it provides integration points for user-task embedding / UI-composition. Unfortunately,
this topic strongly depends on the frontend technology and is not a subject we want to demonstrate
in this example. For simplicity, we built a very simple example, skipping the UI composition / integration entirely.

The navigation between the process platform and process application is just as simple as a full page
navigation of a hyperlink.
