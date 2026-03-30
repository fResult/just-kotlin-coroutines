package com.fResult.com.fResult.coroutines

import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.time.delay
import org.slf4j.LoggerFactory

object SuspendFunctions {
  private val LOGGER = LoggerFactory.getLogger(this::class.java)

  @Suppress("ktlint:standard:max-line-length")
  suspend fun takeTheBus() { // this code can run on a coroutines
    LOGGER.info("Getting in the bus")
    (0..10).forEach {
      LOGGER.info("{}% done") { it * 10 }
      delay(0.3.seconds.toJavaDuration()) // yielding point - coroutines that runs this code can be SUSPENDED
      // cooperative scheduling
    } // yielding point - coroutine is SUSPENDED

    LOGGER.info("Getting off the bus, I'm done")
  }

  // Rules:
  // 1. suspend functions CANNOT be run from regular functions

  // continuation = state of the code at the point a coroutine is suspended
  suspend fun demoSuspendedCoroutine() {
    LOGGER.info("Starting to run some code")

    val resumedComputation =
      suspendCancellableCoroutine { continuation ->
        LOGGER.info("This runs when I'm suspended")
        continuation.resumeWith(Result.success(42))
      }

    LOGGER.info("This prints AFTER resuming the coroutine: $resumedComputation")
  }

  // TODO: why does it not work with suspend fun main in the object?
  // @JvmStatic
  // suspend fun main(args: Array<String>) {
  //   takeTheBus()
  // }
}

suspend fun main(args: Array<String>) {
  // SuspendFunctions.takeTheBus()
  SuspendFunctions.demoSuspendedCoroutine()
}
