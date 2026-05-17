package com.fResult.aktors

import com.fResult.aktors.lib.ActorSystem
import com.fResult.aktors.lib.Behavior
import com.fResult.aktors.lib.Behaviors

object WordCounterStateless {
  operator fun invoke(): Behavior<String> =
    Behaviors.setup { ctx ->
      ctx.log.info("Setting up word counter (Stateless)")
      // Behaviors.same()
      return@setup active()
    }

  private fun active(currentCount: Int = 0): Behavior<String> =
    Behaviors.receiveMessage { ctx, message ->
      val newCount = message.split(" ").size
      val newTotal = newCount + currentCount
      ctx.log.info("received new message, updated count to $newTotal")
      return@receiveMessage active(newTotal)
    }
}

object StatelessActorDemo {
  suspend fun main() {
    ActorSystem.app(WordCounterStateless(), "StatelessWordCounterSystem") { guardianActor ->
      guardianActor `!` "This is an actor framework on top of coroutines"
      guardianActor `!` "Coroutines rock"
    }
  }
}

suspend fun main() {
  StatelessActorDemo.main()
}
