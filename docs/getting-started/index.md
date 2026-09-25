---

title: Integration Guide
---

This guide describes the steps required to configure an existing Camunda BPM Spring Boot process application and
connect it to an **existing Process Platform**.

!!! note
    These steps assume that you have already chosen a deployment scenario and set up the **Core components**. This is a prerequisite for the steps below.


## Add dependency to Polyflow integration starter

Apart from the example application, you might be interested in integrating Polyflow Taskpool and Datapool into your existing
application. To do so, you need to enable your Camunda BPM process engine to use the library.
Add the `polyflow-integration-camunda-bpm-springboot-starter` library. In Maven, add the following dependency
to your `pom.xml`:

``` xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-camunda-bpm-springboot-starter</artifactId>
  <version>${polyflow.version}</version>
</dependency>
```

## Activate Polyflow support

Then find your Spring Boot application class and add the annotation:


``` java
@SpringBootApplication
@EnableTaskpoolEngineSupport
public class MyApplication {

  public static void main(String... args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```

## Configure Polyflow provisioning

Finally, add the following block to your `application.yml`:


``` yaml

camunda:
  bpm:
    default-serialization-format: application/json
    history-level: full
    eventing:
      task: false

polyflow:
  integration:
    client:
      camunda:
        application-name: ${spring.application.name}  # default
    collector:
      camunda:
        application-name: ${spring.application.name}  # default
        process-instance:
          enabled: true
        process-definition:
          enabled: true
        process-variable:
          enabled: true
        task:
          enabled: true
          enricher:
            type: processVariables
    sender:
      enabled: true
      data-entry:
        enabled: true
        type: simple
        application-name: ${spring.application.name}  # default
      process-definition:
        enabled: true
      process-instance:
        enabled: true
      process-variable:
        enabled: true
      task:
        enabled: true
        type: tx
        send-within-transaction: true # Must be set to true in single node scenario.
    form-url-resolver:
        defaultTaskTemplate:  "/tasks/${formKey}/${id}?userId=%userId%"
        defaultApplicationTemplate: "http://localhost:${server.port}/${applicationName}"
        defaultProcessTemplate: "/${formKey}?userId=%userId%"

```

Start your process engine. When it reaches a user task, the console should show that the task was passed to the task pool.

For details about the available configuration options, see [Polyflow Components](../reference-guide/components/).
