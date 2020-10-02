package io.holunda.camunda.taskpool.example.tasklist

import io.holunda.camunda.taskpool.view.query.data.DataEntriesQueryResult
import org.axonframework.common.Registration
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseType
import org.axonframework.queryhandling.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Configuration of the task list example component.
 */
@Configuration
@ComponentScan
class TasklistConfiguration

