## Persistence

If you use relational databases for your Event Store of the [DataPool](../components/core-datapool.md) or [TaskPool](../components/core-taskpool.md) or your view,
using the [JPA View](../components/view-jpa.md), Axon Framework, used as a component of Polyflow will detect and autoconfigure itself. Especially, if you use
Spring Data JPA or Spring JDBC, Axon auto-configuration will try to reuse it. 

If you are using `@EntityScan` annotation, you need to add Axon entities to the scan. To do so, please the following code on top of a class marked with `@Configuration`.

```kotlin
@Configuration
@EntityScan(
  basePackageClasses = [
    TokenEntry::class, DomainEventEntry::class, SagaEntry::class
  ]
)
class MyConfiguration 
```
