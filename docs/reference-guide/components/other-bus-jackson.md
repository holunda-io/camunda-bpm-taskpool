### Purpose

This helper component is for Axon buses (command, event, and query) configured to use Jackson for message serialization.
It provides Jackson modules for serialising classes used by Polyflow.

#### Configuration and Usage

To use the component, add the following dependency to your classpath:

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

If you are not using Jackson to serialise Axon messages (commands, events, and queries), no further configuration is required.

To use Jackson as the Axon message-serialization format, add the following application properties:

```yaml
axon:
  serializer:
    events: jackson
    messages: jackson
    general: jackson 
```

In addition, configure the `ObjectMapper` used by Axon Framework:

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
