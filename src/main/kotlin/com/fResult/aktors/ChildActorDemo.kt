package com.fResult.aktors

import com.fResult.aktors.lib.ActorRef
import com.fResult.aktors.lib.Behavior
import com.fResult.aktors.lib.Behaviors

class ChildActorDemo {
  sealed interface Command

  data class CreateChild(
    val name: String,
  ) : Command

  data class TellChild(
    val message: String,
  ) : Command

  data object StopChild : Command

  object Parent {
    operator fun invoke(): Behavior<Command> = idle()

    private fun idle(): Behavior<Command> =
      Behaviors.receiveMessage { ctx, message ->
        return@receiveMessage when (message) {
          is CreateChild -> {
            ctx.log.info("[parent] Creating child with name ${message.name}")
            val childRef = ctx.spawn(message.name, Child())
            withChild(childRef)
          }

          else -> {
            ctx.log.info("[parent]: I don't recognize this message: $message")
            Behaviors.same()
          }
        }
      }

    private fun withChild(childRef: ActorRef<String>): Behavior<Command> = TODO()
  }

  object Child {
    operator fun invoke(): Behavior<String> =
      Behaviors.receiveMessage { ctx, message ->
        ctx.log.info("[child] I've received $message")
        Behaviors.same()
      }
  }

  // parent actor
  // child actor
}
