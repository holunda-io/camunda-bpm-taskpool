# Sample Approval Application

The following application is an example demonstrating the usage of the Camunda BPM Taskpool. The application is 
build as a SpringBoot process application and shows a simple approval process.

## Preparations 

Before you begin, please build the entire project with `mvn clean install` from the command line. 

Before starting the application, please start the Axon Server. The easiest way to do so is to run:
`docker run -d --name my-axon-server -p 8024:8024 -p 8124:8124 axoniq/axonserver`. To verify t is running,
open your browser [http://localhost:8024/](http://localhost:8024/).

The demo application consists of several Maven modules. In order to start the example, you will need to start only three
of them: 
  - h2 (external database)
  - process-application (example application)
  - taskpool-application
 
The modules can be started by running `mvn spring-boot:run` from command line in the corresponding directories. 

The example taskpool and process applications both provide REST APIs for interaction and offers the Swagger UI for easier usage. 
After starting the application, simply open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
and [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) to use Swagger UI. 

Since the process application includes Camunda BPM engine, you can use the standard Camunda webapps by navigating to [http://localhost:8080/](http://localhost:8080/).
The default user and password are `admin / admin `. 

The taskpool application consists of the major taskpool components and delivers the example tasklist.
To use the example tasklist, please call [http://localhost:8081/tasklist/](http://localhost:8081/tasklist/)
in your browser.

## Tasklist

The currently implemented tasklist is a simple application (implemented as a single-page-application based on Angular 6) that shows the list of taks
available in the task pool. In doing so it provides the ability to filter, sort and page tasks with correlated business events. Here is how it looks like now:

![Angular Task List Classic](tasklist-angular/docs/tasklist-angular-classic.png "Angualar Tasklist, classic representation of tasks")
![Angular Task List Data](tasklist-angular/docs/tasklist-angular-data.png "Angualar Tasklist, representation of tasks with correlated data")

### Features

  - Lists tasks in the system for a dummy user (kermit)
  - Tasks include information about the process, name, description, create time, due date, prio and assignment.
  - Tasks include process data (from process instance)
  - Tasks include correlated business data
  - The tasklist is sortable
  - The list is paged (7 items per page)
  - Claiming / Unclaiming
  - Jump to form

### Ongoing / TODO

  - Filtering 


## Architecture

The following storyboard can be used to understand the Camunda BPM Taskpool mechanics. The following sample
approval process has been implemented:

![Approval Process](process-application/src/main/resources/process_approve_request.png)

### Business context 
Imagine a system that responsible for management of all requests in the company. Using this system, you can submit requests which then get 
eventually approved or rejected. Sometimes, the approver doesn't approve or reject but returns the request back to the originator (that is the person,
who submitted the request). Then, the originator can amend the request and resubmit it or cancel the request. 

### Technical context
Imagine the company has the system for request management at place (it could be an ERP system) which is the primary contact point for request storage.
A request can be created, loaded and modified by this system. Our exmple application integrates with this system and starts a process as soon as a new request
has been submitted.   

## Storyboard

1. Create a new request in the Request System. For doing so, send a POST request to the 
`request-controller` (`POST http://localhost:8080/request`). This call creates a dummy request, for 
salary increase (by kermit for piggy for 10000 USD), which is stored in the request system and start the approval process. 
As a result you will receive the id of the request, acting as a business key of the started process instance:

          AR-b0ba8bc9-5855-4bfb-9575-474924566165

2. The approval process will execute, load the request from the request system, create a user task `Approve Request` 
which can be either approved, rejected or returned to the originator and will assign it to some users. 
In order to see the list of available user tasks, send a GET request to the `task-controller` by providing an empty
filter criteria `[]` (`GET http://localhost:8081/tasklist/rest/tasks?filter=%5B%5D`) and 
you will receive a list of tasks with all supplied information available in the task pool:

        [
          {
            "task": {
              "id": "e2e8db5b-de93-11e8-9a28-36409677cad1",
              "name": "Approve Request",
              "description": "Please approve request AR-b0ba8bc9-5855-4bfb-9575-474924566165 from kermit on behalf of piggy",
              "url": "http://localhost:8080/example-process-approval/tasks/approve-request/id/e2e8db5b-de93-11e8-9a28-36409677cad1",
              "formKey": "approve-request",
              "candidateGroups": [
                "muppetshow"
              ],
              "candidateUsers": [
                "gonzo",
                "fozzy"
              ],
              "assignee": null,
              "processName": "Request Approval",
              "createTime": "2018-11-02T11:38:58.327Z",
              "dueDate": "2019-06-26T07:55:00Z",
              "businessKey": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
              "priority": 23,
              "payload": {
                "request": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
                "originator": "kermit"
              }
            },
            "dataEntries": [
              {
                "entryType": "io.holunda.camunda.taskpool.example.ApprovalRequest",
                "entryId": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
                "payload": {
                  "amount": 10000,
                  "currency": "USD",
                  "id": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
                  "subject": "Salary increase",
                  "applicant": "piggy"
                }
              }
            ]
          }
        ]

3. Let's assume the salary increase request is too high and we want to inform kermit about this. We are not rejecting the 
request completely, but returning it to back to kermit with the comment, that this year, the salary increase limit is 7500 USD.
To do so send your decision to the `process-controller` by providing the business key, the decision and the comment
 (`POST http://localhost:8080/request/AR-b0ba8bc9-5855-4bfb-9575-474924566165/decision/RETURN`). The process will create
a new user task `Amend Request` for the originator, who is kermit.

4. Kermit will change the amount of the request in the "legacy" application and re-submit the request. For doing so, let's 
first receive the request from the `request-controller` (`GET http://localhost/request/AR-b0ba8bc9-5855-4bfb-9575-474924566165`) 
and then send the modified request back (`POST http://localhost/request/AR-b0ba8bc9-5855-4bfb-9575-474924566165`) containing the updated data
structure as payload.

        {
          "id": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
          "applicant": "piggy",
          "subject": "Salary increase",
          "amount": 7499,
          "currency": "USD"
        }

5. Now, after the request is changed, we just resubmit it by sending the following request to the `process-controller`: 
(`POST http://localhost/request/AR-b0ba8bc9-5855-4bfb-9575-474924566165/action/RESUBMIT`). The process will load the latest
version of the request from the request system and create a new approval task.

6. Again, let's retrieve the tasks from the task pool with with special filter criteria `applicant=piggy` (`GET http://localhost:8080/tasklist/rest/tasks?filter=applicant%3Dpiggy`) and  and this time approve the salary increase (`POST http://localhost/request/AR-b0ba8bc9-5855-4bfb-9575-474924566165/decision/APPROVE`).

        [
          {
            "task": {
              "id": "337bd734-de94-11e8-9a28-36409677cad1",
              "name": "Amend\nApproval Request\n",
              "description": null,
              "url": "http://localhost:8080/example-process-approval/tasks/approve-request-two/id/337bd734-de94-11e8-9a28-36409677cad1",
              "formKey": "approve-request-two",
              "candidateGroups": [],
              "candidateUsers": [],
              "assignee": "kermit",
              "processName": "Request Approval",
              "createTime": "2018-11-02T11:41:13.508Z",
              "dueDate": null,
              "businessKey": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
              "priority": 50,
              "payload": {
                "request": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
                "comment": "Request limit this year is 7500 USD."
              }
            },
            "dataEntries": [
              {
                "entryType": "io.holunda.camunda.taskpool.example.ApprovalRequest",
                "entryId": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
                "payload": {
                  "amount": 7499,
                  "currency": "USD",
                  "id": "AR-b0ba8bc9-5855-4bfb-9575-474924566165",
                  "subject": "Salary increase",
                  "applicant": "piggy"
                }
              }
            ]
          }
        ]



## REST API

<table>
<tr>
  <th>Application</th><th>URL</th><th>HTTP Method</th><th>Body</th><th>Response</th><th>Purpose</th>
</tr>
<tr>
  <td>Process</td>
  <td>/request</td><td>POST</td><td>-</td><td>Request id</td><td>Creates a new dummy request, stores it in 
  the legacy application and starts the approval process.</td>
</tr>
<tr>
  <td>Process</td>
  <td>/request/{id}</td><td>GET</td><td>-</td><td>Request</td><td>Retrieves the request by id.</td>
</tr>
<tr>
  <td>Process</td>
  <td>/request/{id}</td><td>POST</td><td>Request</td><td>-</td><td>Changes the request by id.</td>
</tr>
<tr>
  <td>Process</td>
  <td>/request/{id}/decision/{DECISION}</td><td>POST</td><td>Comment</td><td>-</td><td>Completes "Approve Request" task for request with id {id} with the decision {DECISION}, where
  decision should be one of:
  <ul><li>APPROVE: approve request.</li><li>REJECT: reject the request.</li><li>RETURN: return to originator.</li></ul></td>
</tr>
<tr>
  <td>Process</td>
  <td>/request/{id}/action/{ACTION}</td><td>POST</td><td>-</td><td>-</td><td>Completes "Amend Request" task for request with id {id} with the action {ACTION}, where
  action should be one of:
  <ul><li>CANCEL: cancels the request.</li><li>RESUBMIT: re-submits the request.</li></ul></td>
</tr>
<tr>
  <td>Taskpool</td>
  <td>/tasks?filter=[filterCriteria]</td><td>GET</td><td>-</td><td>List of tasks</td><td>JSON describing tasks available for user specified by filter criteria</td>
</tr>
</table>
