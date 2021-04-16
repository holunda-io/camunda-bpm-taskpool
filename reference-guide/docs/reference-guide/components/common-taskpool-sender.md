## Taskpool Sender


### Purpose

### Features

### Usage and configuration

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-taskpool-sender</artifactId>
  <version>${camunda-taskpool.version}</version>
</dependency>
```

Then activate the taskpool sender by providing the annotation on any Spring Configuration:

```java

@Configuration
@EnableTaskpoolSender
class MyDataEntryCollectorConfiguration {

}

```

### Command transmission

#### Handling command transmission

### Message codes

> Please note that the logger root hierarchy is `io.holunda.camunda.taskpool.sender`

Message Code  | Severity  | Logger*  | Description   | Meaning                
--- | --- | :--- | :--- | :--- 
`SENDER-001` | `DEBUG`     | `.gateway`  | Sending command over gateway disabled by property. Would have sent command `payload`. | Sending of any commands is disabled.  
`SENDER-002` | `DEBUG`     | `.gateway`  | Successfully submitted command `payload`.   | Logging the successfully sent command.  
`SENDER-003`    | `ERROR`     | `.gateway`  | Sending command $commandMessage resulted in error   |  Error sending command.  
`SENDER-004`    | `DEBUG`     | `.task`     | Process task sending is disabled by property. Would have sent $command. |  
`SENDER-005`    | `DEBUG`     | `.task`     | Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName |  
`SENDER-006`    | `DEBUG`     | `.task`     | Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName |  
`SENDER-007`    | `DEBUG`     | `.process.definition`     | Process definition sending is disabled by property. Would have sent $command. |  
`SENDER-007`    | `DEBUG`     | `.process.instance`     | Process instance sending is disabled by property. Would have sent $command. |  
`SENDER-009`    | `DEBUG`     | `.process.variable`     | Process variable sending is disabled by property. Would have sent $command. |  
`SENDER-011`    | `INFO`      |                  | Taskpool task commands will be distributed over command bus.  | 
`SENDER-012`    | `INFO`      |                  | Taskpool task command distribution is disabled by property.  | 
`SENDER-013`    | `INFO`      |                  | Taskpool process definition commands will be distributed over command bus.  | 
`SENDER-014`    | `INFO`      |                  | Taskpool process definition command distribution is disabled by property.  | 
`SENDER-015`    | `INFO`      |                  | Taskpool process instance commands will be distributed over command bus.  | 
`SENDER-016`    | `INFO`      |                  | Taskpool process instance command distribution is disabled by property.  | 
`SENDER-017`    | `INFO`      |                  | Taskpool process variable commands will be distributed over command bus.  | 
`SENDER-018`    | `INFO`      |                  | Taskpool process variable command distribution is disabled by property.  | 



