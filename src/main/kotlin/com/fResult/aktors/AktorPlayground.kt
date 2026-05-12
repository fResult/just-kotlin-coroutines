package com.fResult.aktors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AktorPlayground {
  val log: Logger = LoggerFactory.getLogger(javaClass)
  val loggingBehavior =
    Behaviors.receiveMessage<String> { message ->
      log.info("Message received: $message")

      Behaviors.same()
    }

  suspend fun main() {
    ActorSystem.app(loggingBehavior, "FirstActorSystem") { guardian ->
      (1..100).forEach { n ->
        guardian `!` "Message: $n"
      }
    }
  }
}

suspend fun main() {
  AktorPlayground.main()
}
