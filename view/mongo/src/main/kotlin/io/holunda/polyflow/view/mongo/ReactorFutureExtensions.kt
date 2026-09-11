package io.holunda.polyflow.view.mongo

import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

internal fun <T : Any> Mono<T>.toNonNullFuture(): CompletableFuture<T> =
  this
    .switchIfEmpty(Mono.error(NoSuchElementException("Mono completed without a value")))
    .toFuture()
    .thenApply { value -> requireNotNull(value) }
