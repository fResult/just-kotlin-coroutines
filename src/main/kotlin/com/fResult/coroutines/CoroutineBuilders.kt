package com.fResult.com.fResult.coroutines

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import org.slf4j.LoggerFactory

object CoroutineBuilders {
  private val LOGGER = LoggerFactory.getLogger(this::class.java)

  suspend fun developer(idx: Int) {
    LOGGER.info("[dev $idx] I'm a developer. I need coffee.")
    delay(Random.nextLong(1000).milliseconds.toJavaDuration()) // can suspend the coroutine
    LOGGER.info("[dev $idx] I got coffee, let's get coding!")
  }

  suspend fun projectManager() {
    LOGGER.info("[PM] I'm a PM. I need to check the devs' progress.")
    delay(Random.nextLong(1000).milliseconds.toJavaDuration()) // can suspend the coroutine
    LOGGER.info("[PM] I checked progress, let's grab lunch")
  }

  fun createDeveloperRoutine(idx: Int): suspend CoroutineScope.() -> Unit = { developer(idx) }

  // Structured Concurrency Demonstration
  suspend fun startup() {
    timeToStartAt("9AM")
    // COROUTINE SCOPE
    coroutineScope {
      // the ability to launch coroutines concurrently
      launch { developer(42) }
      launch(block = createDeveloperRoutine(99))
      launch { projectManager() }
      (1..3).forEach { n -> launch(block = createDeveloperRoutine(n)) }
      // manages suspend lifecycle of coroutine ...
    } // will (semantically) block until coroutines inside finish

    timeToGoHomeAt("6PM")

    LOGGER.info("=============================================")
    LOGGER.info("============... 3 HOURS LATER ...============")
    LOGGER.info("=============================================")

    LOGGER.info("It's 9PM, time to be on call")

    @Suppress("ktlint:standard:no-consecutive-comments")
    coroutineScope {
      launch { developer(99) }
      launch { developer(42) }

      /*
       * these are redundant join (blocking) following suspend the globalStartup function
       * because `coroutineScope` does this automatically
       */
      // val job1 = launch { developer(99) }
      // val job2 = launch { developer(42) }
      // job1.join()
      // job2.join()
    }

    LOGGER.info("It's 5AM, time to sleep")
  }

  // Unstructured Concurrency Demonstration
  @OptIn(DelicateCoroutinesApi::class)
  suspend fun globalStartup() {
    timeToStartAt("10AM")
    // global scope - for the duration of the entire app
    val dev1Job = GlobalScope.launch { developer(1) }
    val dev2Job = GlobalScope.launch { developer(2) }
    // easy to  leak resources on GlobalScope

    // manually join coroutines
    dev1Job.join() // semantically blocking
    dev2Job.join()

    timeToGoHomeAt("7PM")
  }

  private fun timeToStartAt(time: String) = LOGGER.info("It's $time, time to start")

  private fun timeToGoHomeAt(time: String) = LOGGER.info("It's {}, time to go home", time)
}

suspend fun main() {
  // CoroutineBuilders.startup()
  CoroutineBuilders.globalStartup()
}
