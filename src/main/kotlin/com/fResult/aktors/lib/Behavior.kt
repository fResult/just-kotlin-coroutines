package com.fResult.aktors.lib

sealed interface Behavior<in T> {
  fun <S : T> ifSameThen(other: Behavior<S>): Behavior<S> = this
}

object Behaviors {
  fun <T> receiveMessage(handler: (T) -> Behavior<T>): Behavior<T> = ReceiveMessage(handler)

  fun <T> setup(initialization: (ActorContext<T>) -> Behavior<T>) = Setup(initialization)

  fun <T> same(): Behavior<T> = Same

  fun <T> stopped(): Behavior<T> = Stopped

  class ReceiveMessage<T>(
    val handler: (T) -> Behavior<T>,
  ) : Behavior<T>

  class Setup<T>(
    val initialization: (ActorContext<T>) -> Behavior<T>,
  ) : Behavior<T>

  data object Same : Behavior<Any?> {
    override fun <S> ifSameThen(other: Behavior<S>) = other
  }

  data object Stopped : Behavior<Any?>
}

/*
 * Behaviors.receiveMessage { message ->
 *   log.info(message)
 *
 *   if (message.length > 5)
 *     Behaviors.same() // behavior is unchanged
 *   else Behaviors.receiveMessage { msg ->
 *     post to Twitter
 *   }
 * }
 */
