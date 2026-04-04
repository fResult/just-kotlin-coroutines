package com.fResult.com.fResult.coroutines

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import org.slf4j.LoggerFactory

object CoroutineBuilders {
  private val LOGGER = LoggerFactory.getLogger(this::class.java)

  suspend fun developer(idx: Int) {
    LOGGER.info("[dev $idx] I'm a developer. I need coffee.")
    delay(Random.nextLong(1000).milliseconds.toJavaDuration())
    LOGGER.info("[dev $idx] I got coffee, let's get coding!")
  }

  suspend fun projectManager() {
    LOGGER.info("[PM] I'm a PM. I need to check the devs' progress.")
    delay(Random.nextLong(1000).milliseconds.toJavaDuration())
    LOGGER.info("[PM] I checked progress, let's grab lunch")
  }

  fun createDeveloperRoutine(idx: Int): suspend CoroutineScope.() -> Unit =
    {
      developer(idx)
    }

  suspend fun startup() {
    LOGGER.info("It's 9AM, let's start")
    // COROUTINE SCOPE
    coroutineScope {
      // the ability to launch coroutines concurrently
      launch { developer(42) }
      launch(block = createDeveloperRoutine(99))
      (1..3).forEach { n -> launch(block = createDeveloperRoutine(n)) }
      launch { projectManager() }
      // manages suspend lifecycle of coroutine ...
    } // will (semantically) block until coroutines inside finish

    LOGGER.info("It's 6PM, time to go home")
  }
}

suspend fun main() {
  CoroutineBuilders.startup()
}
