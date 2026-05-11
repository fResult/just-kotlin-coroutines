package com.fResult.aktors

object AktorPlayground {
  suspend fun main() {
    ActorSystem.app("FirstActorSystem") { guardian ->
      (1..100).forEach { n ->
        guardian `!` "Message: $n"
      }
    }
  }
}

suspend fun main() {
  AktorPlayground.main()
}
