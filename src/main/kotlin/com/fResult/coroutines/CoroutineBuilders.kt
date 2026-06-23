package com.fResult.com.fResult.coroutines

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

object CoroutineBuilders {
  private val LOGGER = LoggerFactory.getLogger(this::class.java)

  suspend fun developer(idx: Int) {
    developerNeedCoffee(idx)
    delay(Random.nextLong(1000).milliseconds) // can suspend the coroutine
    developerGotCoffee(idx)
  }

  suspend fun projectManager() {
    pmCheckProgress()
    delay(Random.nextLong(1000).milliseconds) // can suspend the coroutine
    pmFinishedCheckingProgress()
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
    // easy to leak resources on GlobalScope

    // manually join coroutines
    dev1Job.join() // semantically blocking
    dev2Job.join()

    timeToGoHomeAt("7PM")
  }

  // async - return a value out of a coroutine
  suspend fun developerCoding(idx: Int): String {
    developerNeedCoffee(idx)
    delay(Random.nextLong(1000).milliseconds) // can suspend the coroutine
    developerGotCoffee(idx)

    return """
      fun main() { println("This is KOTLIN!") }
      """.trimIndent()
  }

  suspend fun projectManagerEstimating(): String {
    pmCheckProgress()
    delay(Random.nextLong(1000).milliseconds) // can suspend the coroutine
    pmFinishedCheckingProgress()

    return "12 Hours"
  }

  data class Feature(
    val code: String,
    val estimation: String,
  )

  suspend fun startupValues() {
    timeToStartAt("9AM")
    val feature =
      coroutineScope {
        val deferredCode = async { developerCoding(42) }
        val deferredEstimation = async { projectManagerEstimating() }

        val code = deferredCode.await() // semantically blocking
        val estimation = deferredEstimation.await()

        return@coroutineScope Feature(code, estimation)
      }

    LOGGER.info("It's 9PM, still going. We have the feature {}", feature)
  }

  private fun timeToStartAt(time: String) = LOGGER.info("It's $time, time to start")

  private fun timeToGoHomeAt(time: String) = LOGGER.info("It's {}, time to go home", time)

  private fun developerNeedCoffee(n: Int) = LOGGER.info("[dev $n], I'm a developer. I need coffee.")

  private fun developerGotCoffee(n: Int) = LOGGER.info("[dev $n], I got coffee, let's get coding!")

  private fun pmCheckProgress() = LOGGER.info("[PM] I'm a PM. I need to check the devs' progress.")

  private fun pmFinishedCheckingProgress() =
    LOGGER.info("[PM] I checked progress, let's grab lunch.")
}

suspend fun main() {
  // CoroutineBuilders.startup()
  // CoroutineBuilders.globalStartup()
  CoroutineBuilders.startupValues()
}
