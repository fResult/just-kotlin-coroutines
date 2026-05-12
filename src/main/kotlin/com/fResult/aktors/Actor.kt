package com.fResult.aktors

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.yield
import org.slf4j.LoggerFactory

/*
 * - name and channel args
 * - `run()` method which pops elements off the channel and logs them
 */
internal class Actor<T>(
  private val name: String,
  private val channel: Channel<T>,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  suspend fun run(startBehavior: Behavior<T>) {
    var behavior = startBehavior
    var newBehavior = behavior
    while (true) {
      when (behavior) {
        is Behaviors.ReceiveMessage -> {
          val message = channel.receive()
          val handle = behavior.handler
          newBehavior = handle(message)
        }
        is Behaviors.Same ->
          throw IllegalStateException(
            """
            The INSTANCE 'Behaviors.Same' is illegal, probably a bug in the code
            """.trimIndent(),
          )
      }

      behavior = maybeTransition(behavior, newBehavior)
      newBehavior = behavior
      yield() // the suspension point is important
    }
  }

  private fun maybeTransition(
    behavior: Behavior<T>,
    newBehavior: Behavior<T>,
  ): Behavior<T> =
    when (newBehavior) {
      is Behaviors.Same -> behavior
      else -> newBehavior
    }
}
