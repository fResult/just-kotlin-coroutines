package com.fResult.aktors

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/*
 * - call `scope.launch - start a new coroutine with a new Actor
 * - call the `run()` on that actor in that coroutine
 * - return an `ActorRef` with the actor's channel
 */
open class ActorScope {
  protected fun <T> createActor(
    name: String,
    scope: CoroutineScope,
    context: CoroutineContext,
  ): ActorRef<T> {
    val mailbox = Channel<T>(capacity = Channel.UNLIMITED) // can configure it
    scope.launch(context) {
      val actor = Actor(name, mailbox)
      actor.run()
    }

    return ActorRef(mailbox)
  }
}
