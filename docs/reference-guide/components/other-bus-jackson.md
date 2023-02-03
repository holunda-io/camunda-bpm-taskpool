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
  @Qualifier("payloadObjectMapper")
  fun payloadObjectMapper(): ObjectMapper {
    return ObjectMapper().configureTaskpoolJacksonObjectMapper()
  }
}
```

If you are not using Jackson for serialization of Axon messages (commands, events and queries) you
are ready to go.

If you want to use Jackson as Axon message serialization message format the following configuration
is required. In your application properties, set-up the following properties:

```yaml
axon:
  serializer:
    events: jackson
    messages: jackson
    general: jackson 
```

In addition, define configure the `ObjectMapper` to be used by Axon Framework:

```kotlin

import io.holunda.polyflow.bus.jackson.configureTaskpoolJacksonObjectMapper

class MyConfiguration {

  @Bean("defaultAxonObjectMapper")
  @Qualifier("defaultAxonObjectMapper")
  fun defaultAxonObjectMapper(): ObjectMapper {
    return ObjectMapper().configureTaskpoolJacksonObjectMapper()
  }
}
```
