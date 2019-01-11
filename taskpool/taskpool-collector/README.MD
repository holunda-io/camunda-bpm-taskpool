# Taskpool Collector User Guide

This user guide describes features of the task collector component, how to use and to configure them.

## Rationale

Taskpool collector is a component deployed on the Camunda BPM engine side that is responsible for
collecting Spring events fired by the `camunda-eventing-engine-plugin` and creating the commands
for the taskpool. In doing so, it collects and enriches events and transmits them to taskpool core.

## Features

- Collection of task events (create, assign, complete, delete)
- Collection of history events
- Creation of corresponding task engine commands
- Enrichment of task engine commands with process variables
- Attachment of correlation information to task engine commands
- Transmission of task engine commands

### Event enrichment

Alongside with data received from the Camunda engine, the task engine commands can be enriched with additional
business data. There are three enrichment modes available 
(controlled by the `camunda.taskpool.collector.enricher.type` property):

* `no`: No enrichment takes place
* `process-variables`: Enrichment with process variables
* `custom`: Custom enrichment

See `TaskCollectorEnricherType` for exact values.

#### Process variable enrichment

In particular cases, the task related data is not sufficient for the information required in task list or
other user-related components. The information may be available as process variables and need to be attached
to the task in the task pool. This is where process variable task enricher can be used. For this purpose,
set the property `camunda.taskpool.collector.enricher.type` to `process-variables` and the enricher will
put all process variables into the task payload (defaults to a empty `EXCLUDE` filter).

You can control what variables will be put into task payload by providing the Process Variables Filter.
The `ProcessVariablesFilter` is a spring bean holding a list individual `ProcessVariableFilter` - one per 
process definition key. 

A `ProcessVariableFilter` can be of the following type:
* `INCLUDE`: task-level include filter, denoting a list of variables to be added for task.
* `EXCLUDE`: task-level exclude filter, denoting a list of variables to be ignored. All other variables are included.
* `PROCESS_INCLUDE`: process-level include filter, denoting a list of variables to be added for all tasks. 
* `PROCESS_EXCLUDE`: process-level exclude filter, denoting a list of variables to be ignored for all tasks.
  
   
#### Custom variable enrichment

If you want to implement a custom enrichment, please provide your own implementation of the following 
interfaces (register a Spring Component of the type):

* `CreateCommandEnricher`
* `AssignCommandEnricher`
* `DeleteCommandEnricher`
* `CompleteCommandEnricher`

and set the property `camunda.taskpool.collector.enricher.type` to `custom`.

### Event correlation

Apart from task payload attached by the enricher, the so-called correlation with data entries can
be configured. The idea is to attach one or several references (that is entryType and entryId) to 
business data entry(ies) to a task. In a view projection this correlations can be resolved and the 
information from business events can be shown together with task information.

The correlation to data events can be configured by providing a `ProcessVariablesCorrelator`. Here is 
an example how this can be done: 

    @Bean
    open fun processVariablesCorrelator() = ProcessVariablesCorrelator(
  
      // define correlation for every process
      ProcessVariableCorrelation(ProcessApproveRequest.KEY,
        mapOf(
          // define a correlation for every task needed
          ProcessApproveRequest.Elements.APPROVE_REQUEST to mapOf(
            ProcessApproveRequest.Variables.REQUEST_ID to BusinessDataEntry.REQUEST
          )
        ),
        // or globally
        mapOf(ProcessApproveRequest.Variables.REQUEST_ID to BusinessDataEntry.REQUEST)
      )
    )

The process variable correlator holds a list of process variable correlations - one for every process
definition key. Every `ProcessVariableCorrelation` configures global (that is for every task) or task correlation 
(for particular task definition key) by providing a correlation map. A correlation map is keyed by the 
process variable name and holds business data entry type as value. Here is an example:

Imagine the process instance is storing the id of an approval request in a process variable called
`varRequestId`. The system responsible for storing approval requests fires data entry events supplying the 
data and using the entry type `approvalRequest` and the id of the request as entryId. In order to
create a correlation in task `task_approve_request` of the `process_approval_process` we would provide the following configuration
of the correlator:

    @Bean
    open fun processVariablesCorrelator() = ProcessVariablesCorrelator(
  
      ProcessVariableCorrelation("process_approval_process",
        mapOf(
          "task_approve_request" to mapOf(
            "varRequestId" to "approvalRequest"
          )
        )
      )
    )
    
If the process instance now contains the approval request id `"4711"` in the process variable `varRequestId`
and the process reaches the task `task_approve_request`, the task will get the following correlation created
(here written in JSON):

    "correlations": [
      { "entryType": "approvalRequest", "entryId": "4711" }
    ]
    
### Command transmission

The Spring event listeners receiving events from the engine plugin are called before Camunda commits the transaction.
Therefor, the sender waits for transaction to commit, prio sending commands to the commnd bus (otherwise, on any error
the transaction would be rolled back and the command would create an inconsistency between the taskpool and the engine).

The property controlling the sender type is `camunda.taskpool.collector.sender.type` takes the values:

* `tx`: (default) Send commands after transaction commit.
* `simple`: Send out commands before transaction commit
* `custom`: Custom handling, a bean implementing `CommandSender` must be provided.




