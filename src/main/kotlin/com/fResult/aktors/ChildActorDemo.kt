package com.fResult.aktors

import com.fResult.aktors.lib.Behavior

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
    operator fun invoke(): Behavior<Command> = TODO()

    private fun idle(): Behavior<Command> = TODO()
  }

  // parent actor
  // child actor
}
