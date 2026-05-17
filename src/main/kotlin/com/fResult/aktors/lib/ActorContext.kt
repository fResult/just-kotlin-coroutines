package com.fResult.aktors.lib

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope

class ActorContext<T>(
  val self: ActorRef<T>,
  val name: String,
  val scope: CoroutineScope,
  val context: CoroutineContext,
): ActorScope() {
  fun <S> spawn(name: String, behavior: Behavior<S>): ActorRef<S> = TODO()
}

/*
 * data class Command(payload: String, replyTo: ActorRef<T>)
 *
 * Behaviors.setup { context ->
 *   // logging
 *   ctx.log(...)
 *   // spawning a child actor
 *   val childRef = ctx.spawn(name, behavior)
 *
 *   // ... later ...
 *   childRef `!` Command("some message", ctx.self)
 *
 *   // request-response
 *   // asking and getting a value
 *   // pipe items
 * }
 */
