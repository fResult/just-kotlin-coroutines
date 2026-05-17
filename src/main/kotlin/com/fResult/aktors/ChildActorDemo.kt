package com.fResult.aktors

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
        // TODO
        Behaviors.same()
      }
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
