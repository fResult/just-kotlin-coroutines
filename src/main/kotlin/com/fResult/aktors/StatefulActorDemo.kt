package com.fResult.aktors

import com.fResult.aktors.lib.ActorSystem
import com.fResult.aktors.lib.Behavior
import com.fResult.aktors.lib.Behaviors

object WordCounterStateful {
  operator fun invoke(): Behavior<String> =
    Behaviors.setup { ctx ->
      ctx.log.info("Setting up word counter (Stateful)")
      var total = 0

      // Behaviors.same() // should stop the actor
      return@setup Behaviors.receiveMessage { message ->
        val newCount = message.split(" ").size
        total += newCount
        ctx.log.info("received new message, updated count to $total")
        return@receiveMessage Behaviors.same()
      }
    }
}

object StatefulActorDemo {
  /*
   * actorRef.tell("This is an actor framework on coroutines") -> wc = 7
   * actorRef.tell("Coroutines rock") -> wc = 9
   */
  suspend fun main() {
    ActorSystem.app(WordCounterStateful(), "StatefulWordCounterSystem") { guardianActor ->
      guardianActor `!` "This is an actor framework on top of coroutines"
      guardianActor `!` "Coroutines rock"
    }
  }
}

suspend fun main() {
  StatefulActorDemo.main()
}
