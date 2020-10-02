package io.holunda.camunda.taskpool.example.process.service.projection

import io.holunda.camunda.taskpool.view.query.RevisionQueryParameters
import io.holunda.camunda.taskpool.view.query.Revisionable
import mu.KLogging
import org.axonframework.common.Registration
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.IllegalPayloadAccessException
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.messaging.responsetypes.ResponseType
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.util.concurrent.Queues
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeoutException

@Configuration
class SyncGatewayConfiguration {
  @Bean
  fun syncGateway(queryBus: QueryBus): QueryGateway {
    return SyncQueryGateway(queryBus, 20_000)
  }
}

/**
 * Synchronizing gateway taking care of a revision of the projection requested by the query and will deliver
 * results matching at leas this revision.
 * @param queryBus bus to use.
 * @param defaultTimeout default timeout to use if not specified in the query.
 */
class SyncQueryGateway(
  private val queryBus: QueryBus,
  private val defaultTimeout: Long
) : DefaultQueryGateway(builder().queryBus(queryBus)) {

  private val dispatchInterceptors: MutableList<MessageDispatchInterceptor<in QueryMessage<*, *>?>> = CopyOnWriteArrayList()

  companion object : KLogging()

  @Suppress("UNCHECKED_CAST")
  override fun <R : Any, Q : Any> query(queryName: String, query: Q, responseType: ResponseType<R>): CompletableFuture<R> {

    val queryMessage: Message<Q> = GenericMessage.asMessage(query) as Message<Q>
    val revisionQueryParameter = RevisionQueryParameters.fromMetaData(metaData = queryMessage.metaData)

    return if (revisionQueryParameter == RevisionQueryParameters.NOT_PRESENT) {
      super.query(queryName, query, responseType)
    } else {
      val result = CompletableFuture<R>()
      logger.debug { "Revision-aware query: $revisionQueryParameter" }

      val queryTimeout = revisionQueryParameter.getTimeoutOrDefault(defaultTimeout)

      val subscriptionQueryMessage: SubscriptionQueryMessage<Q, R, R> = GenericSubscriptionQueryMessage(
        queryMessage,
        queryName,
        ResponseTypes.instanceOf(responseType.expectedResponseType) as ResponseType<R>,
        ResponseTypes.instanceOf(responseType.expectedResponseType) as ResponseType<R>
      )

      val queryResult: SubscriptionQueryResult<QueryResponseMessage<R>, SubscriptionQueryUpdateMessage<R>> = queryBus
        .subscriptionQuery(processInterceptors(subscriptionQueryMessage), SubscriptionQueryBackpressure.defaultBackpressure(), Queues.SMALL_BUFFER_SIZE)

      queryResult
        .initialResult()
        .filter { initialResult: QueryResponseMessage<R> -> Objects.nonNull(initialResult.payload) }
        .map { obj: QueryResponseMessage<R> -> obj.payload }
        .onErrorMap { e: Throwable -> if (e is IllegalPayloadAccessException) e.cause else e }
        .concatWith(queryResult
          .updates()
          .filter { update: SubscriptionQueryUpdateMessage<R> -> Objects.nonNull(update.payload) }
          .map { obj: SubscriptionQueryUpdateMessage<R> -> obj.payload }
        )
        .map {
          println("-----------------------> Element $it")
          it
        }
        .timeout(
          Duration.of(queryTimeout, ChronoUnit.SECONDS)) {
          GenericQueryResponseMessage
            .asResponseMessage(responseType.responseMessagePayloadType(), TimeoutException("Could not find requested ${revisionQueryParameter.minimalRevision} revision during $queryTimeout"))
        }
        .filter { it is Revisionable }
        .map { it to (it as Revisionable).revisionValue }
        .filter { pair -> pair.second.revision >= revisionQueryParameter.minimalRevision }
        .map {
          println("Projection ordinal: ${it.second}")
          it.first
        }
        .subscribe { projectionResult -> result.complete(projectionResult) }

      result
    }
  }

  override fun registerDispatchInterceptor(interceptor: MessageDispatchInterceptor<in QueryMessage<*, *>?>): Registration {
    dispatchInterceptors.add(interceptor)
    return Registration { dispatchInterceptors.remove(interceptor) }
  }

  private fun <Q, R, T : QueryMessage<Q, R>> processInterceptors(query: T): T {
    var intercepted: T = query
    for (interceptor in dispatchInterceptors) {
      @Suppress("UNCHECKED_CAST")
      intercepted = interceptor.handle(intercepted) as T
    }
    return intercepted
  }

}

// FIXME -> currently unused. remove...
//class QueryResponseMessageResponseType<T : Any> : AbstractResponseType<T> {
//
//  companion object {
//    @JvmStatic
//    inline fun <reified T : Any> queryResponseMessageResponseType() = QueryResponseMessageResponseType(T::class)
//  }
//
//  @JsonCreator
//  @ConstructorProperties("expectedResponseType")
//  constructor(@JsonProperty("expectedResponseType") clazz: KClass<T>) : super(clazz.java)
//
//  override fun matches(responseType: Type): Boolean {
//    val unwrapped = ReflectionUtils.unwrapIfType(responseType, QueryResponseMessage::class.java)
//    return isGenericAssignableFrom(unwrapped) || isAssignableFrom(unwrapped)
//  }
//
//  @SuppressWarnings("unchecked")
//  @Suppress("UNCHECKED_CAST")
//  override fun responseMessagePayloadType(): Class {
//    return expectedResponseType as Class<T>
//  }
//
//  @Suppress("UNCHECKED_CAST")
//  override fun forSerialization(): ResponseType<T> {
//    return ResponseTypes.instanceOf(expectedResponseType as Class<T>)
//  }
//
//  override fun convert(response: Any): T {
//    return super.convert(response)
//  }
//
//  override fun toString(): String {
//    return "QueryResponseMessageResponseType{$expectedResponseType}"
//  }
//
//
//}
