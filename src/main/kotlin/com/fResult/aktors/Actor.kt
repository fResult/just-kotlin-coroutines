package com.fResult.aktors

import kotlinx.coroutines.Job
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
  private val job: Job,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  suspend fun run(startBehavior: Behavior<T>) {
    log.info("Starting actor $name")
    var behavior = startBehavior
    var newBehavior = behavior
    while (true) {
      when (behavior) {
        is Behaviors.Setup -> {
          newBehavior = behavior.initialization()
          // if behaviors.same -> stopped behavior
          behavior = newBehavior.ifSameThen(Behaviors.stopped())
        }

        is Behaviors.ReceiveMessage -> {
          val message = channel.receive()
          val handle = behavior.handler
          newBehavior = handle(message)
          behavior = newBehavior.ifSameThen(behavior)
        }

        is Behaviors.Same ->
          throw IllegalStateException(
            """
            The INSTANCE 'Behaviors.Same' is illegal, probably a bug in the code
            """.trimIndent(),
          )

        Behaviors.Stopped -> {
          channel.close() // prevent other coroutines from sending new messages
          // TODO - what do you do with messages that arrive at this (closed) channel?
          // dead letters - receives any message that don't have a valid destination
          job.cancel()
        }
      }

      yield() // the suspension point is important
    }
  }
}
