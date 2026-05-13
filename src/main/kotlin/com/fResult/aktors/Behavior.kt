package com.fResult.aktors

sealed interface Behavior<T>

object Behaviors {
  fun <T> receiveMessage(handler: (T) -> Behavior<T>): Behavior<T> = ReceiveMessage(handler)

  fun <T> setup(initialization: () -> Behavior<T>) = Setup(initialization)

  @Suppress("UNCHECKED_CAST")
  fun <T> same(): Behavior<T> = Same as Behavior<T>

  class ReceiveMessage<T>(
    val handler: (T) -> Behavior<T>,
  ) : Behavior<T>

  class Setup<T>(
    val initialization: () -> Behavior<T>,
  ) : Behavior<T>

  data object Same : Behavior<Nothing>
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
