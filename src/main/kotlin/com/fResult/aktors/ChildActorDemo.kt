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
      Behaviors.receiveMessage { ctx, command ->
        return@receiveMessage when (command) {
          is CreateChild -> {
            ctx.log.info("[parent] Creating child with name ${command.name}")
            val childRef = ctx.spawn(command.name, Child())
            withChild(childRef)
          }

          else -> {
            ctx.log.info("[parent]: I don't recognize this message: $command")
            Behaviors.same()
          }
        }
      }

    private fun withChild(childRef: ActorRef<String>): Behavior<Command> =
      Behaviors.receiveMessage { ctx, command ->
        when (command) {
          is TellChild -> {
            ctx.log.info("[parent] Sending message to my child ${command.message}")
            childRef `!` command.message
            Behaviors.same()
          }

          is StopChild -> {
            ctx.log.info("[parent] Stopping my child")
            StopChild::class.simpleName?.also { childRef `!` it }
            idle()
          }

          is CreateChild -> {
            ctx.log.info("[parent]: I don't recognize this message: $command")
            Behaviors.same()
          }
        }
      }
  }

  object Child {
    operator fun invoke(): Behavior<String> =
      Behaviors.receiveMessage { ctx, message ->
        ctx.log.info("[child] I've received $message")
        when (message) {
          StopChild::class.simpleName -> Behaviors.stopped()
          else -> Behaviors.same()
        }
      }
  }

  // parent actor
  // child actor
}
