package com.fResult.aktors.lib

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.slf4j.LoggerFactory

class ActorContext<T>(
  val self: ActorRef<T>,
  val name: String,
  // infra
  val job: Job, // coroutine of this actor
  val scope: CoroutineScope,
) : ActorScope() {
  val log = LoggerFactory.getLogger(name)

  fun <S> spawn(
    name: String,
    behavior: Behavior<S>,
  ): ActorRef<S> =
    super.createActor(
      behavior,
      name,
      scope,
      buildCoroutineContext(job, name),
    )

  private fun buildCoroutineContext(
    parentJob: Job,
    name: String,
  ): CoroutineContext = parentJob + CoroutineName(name)

  // path /
  // child1 /child1
  // grand-child1 /child1/grand-child1
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
