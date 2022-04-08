## Tasklist URL Resolver

### Purpose

The Tasklist URL Resolver is a helper component helping to provide the URL of the task list for other components. It is not use by other components,
but is helpful, if you complete tasks using SPA on the side of the process application and needs a redirection target resolution of the task list 
after completion.

### Usage and Configuration

To use Tasklist URL Resolver please add the following artifact to your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-camunda-bpm-engine-client</artifactId>
</dependency>
```

In your `application.yml` either configure the property for the static tasklist URL:

```yml
polyflow:
  integration:
    tasklist:
      tasklist-url: http://my-task-list.application.url/
```

or provide your own implementation of the `TasklistUrlResolver` interface as Spring Bean.

