package com.fResult.aktors.playground

import com.fResult.aktors.ActorSystem
import com.fResult.aktors.Behavior
import com.fResult.aktors.Behaviors
import org.slf4j.LoggerFactory

object WordCounter {
  private val log = LoggerFactory.getLogger(javaClass)

  operator fun invoke(): Behavior<String> =
    Behaviors.setup {
      log.info("Setting up word counter")
      var total = 0

      return@setup Behaviors.receiveMessage { message ->
        val newCount = message.split(" ").size
        total += newCount
        log.info("received new message, updated count to $total")
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
    ActorSystem.app(WordCounter(), "WordCounterSystem") { guardianActor ->
      guardianActor `!` "This is an actor framework on top of coroutines"
      guardianActor `!` "Coroutines rock"
    }
  }
}

suspend fun main() {
  StatefulActorDemo.main()
}
