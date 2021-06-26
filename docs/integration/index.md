---

title: Integration Guide
---

This guide is describing steps required to configure an existing Camunda BPM Spring Boot Process Application and
connect to existing Process Platform.


## Add dependency to Polyflow integration starter

Apart from the example application, you might be interested in integrating Polyflow Taskpool and Datapool into your existing
application. To do so, you need to enable your Camunda BPM process engine to use the library.
For doing so, add the `polyflow-integration-camunda-bpm-engine-parent` library. In Maven, add the following dependency
to your `pom.xml`:

``` xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-integration-camunda-bpm-engine-parent</artifactId>
  <version>${polyflow.version}</version>
</dependency>
```

## Activate Polyflow Support

Now, find your SpringBoot application class and add an additional annotation to it:


``` java
@SpringBootApplication
@EnableTaskpoolEngineSupport
public class MyApplication {

  public static void main(String... args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```

## Configure your Polyflow provisioning

Finally, add the following block to your `application.yml`:


``` yaml

camunda:
  bpm:
    default-serialization-format: application/json
    history-level: full

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

Now, start your process engine. If you run into a user task, you should see on the console how this is passed to task pool.

For more details on the configuration of different options, please consult the [Polyflow Components](../reference-guide/components/) sections.
