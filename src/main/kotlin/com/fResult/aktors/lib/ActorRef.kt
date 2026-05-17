package com.fResult.aktors.lib

import kotlinx.coroutines.channels.SendChannel

/*
 * - receives a message of a certain type
 * - wraps a coroutine channel
 * - a method `tell(message: YourType)` -> push an element to that channel
 * - a method `!`
 */
class ActorRef<T> internal constructor(
  private val mailbox: SendChannel<T>,
) {
  suspend fun tell(message: T) {
    mailbox.send(message)
  }

  @Suppress("ktlint:standard:function-naming")
  suspend infix fun `!`(message: T) = tell(message)
}
