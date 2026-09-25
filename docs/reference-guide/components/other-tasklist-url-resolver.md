## Tasklist URL Resolver

### Purpose

The Tasklist URL Resolver is a helper component that provides the task-list URL to other components. It is not used by other components,
but is useful when tasks are completed through an SPA in the process application and a task-list redirect target is needed
after completion.

### Usage and Configuration

To use Tasklist URL Resolver, add the following artifact to your classpath:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-tasklist-url-resolver</artifactId>
</dependency>
```

In your `application.yml` either configure the property for the static tasklist URL:

```yml
polyflow:
  integration:
    tasklist:
      tasklist-url: http://my-task-list.application.url/
```

or provide your own `TasklistUrlResolver` implementation as a Spring bean:

```java

import java.beans.BeanProperty;

@Configuration
class MyConfiguration {

  @Bean
  public TasklistUrlResolver myTasklistUrlResolver() {
      return MyTasklistUrlResolver();
  }
}


```
