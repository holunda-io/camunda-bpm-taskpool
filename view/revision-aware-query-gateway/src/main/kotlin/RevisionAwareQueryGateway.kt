package io.holunda.camunda.taskpool.gateway

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
import reactor.util.concurrent.Queues
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeoutException

/**
 * The revision  gateway taking care of a revision of the projection requested by the query and will deliver
 * results matching at leas this revision.
 * @param queryBus bus to use.
 * @param defaultTimeout default timeout to use if not specified in the query.
 */
class RevisionAwareQueryGateway(
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
        .subscriptionQuery(
          processInterceptors(subscriptionQueryMessage),
          SubscriptionQueryBackpressure.defaultBackpressure(),
          Queues.SMALL_BUFFER_SIZE
        )

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
          logger.debug { "Response received:\n $it" } // FIXME: change severity
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
          logger.info { "Response revision: ${it.second}"} // FIXME: change severity
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
