### Purpose

The component is a helper component if you configure your Axon busses (command, event, query) to use Jackson for serialization of messages.
It provides helper Jackson Modules to configure serialization of classes used by Polyflow. 

#### Configuration and Usage

To use the component, please add the following dependency to your classpath

```xml
<dependency>
  <groupId>io.holunda.taskpool</grouId>
  <artifactId>polyflow-bus-jackson</artifactId>
</dependency>
```

Inside your Object Mapper configuration call

```kotlin

import io.holunda.polyflow.bus.jackson.configureTaskpoolJacksonObjectMapper

class MyConfiguration {
  @Bean
  fun objectMapper(): ObjectMapper {
    return ObjectMapper().configureTaskpoolJacksonObjectMapper()
  }
}

```
