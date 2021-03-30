---

title: User Management
---

## User Management

Usually, a central user management like a Single-Sign-On (SSO) is a part deployed into the process application
landscape. This is responsible for authentication and authorization of the user and is required to control the
role-based access to user tasks.

In our example application, we __disable any security checks__ to avoid the unneeded complexity. In doing so, we
implemented a trivial user store holding some pre-defined users used in example components and allow to simply
switch users from the frontend by choosing a different user from the drop-down list.

It is integrated with the example frontends by passing around the user id along with the requests and provide a way
to resolve the user by that id. Technically speaking, the user id can be used to retrieve the permissions of the user
and check them in the backend.
