package com.fResult.com.fResult.coroutines

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory

object SuspendFunctions {
  private val LOGGER = LoggerFactory.getLogger(this::class.java)

  @Suppress("ktlint:standard:max-line-length")
  suspend fun takeTheBus() { // this code can run on a coroutines
    LOGGER.info("Getting in the bus")
    (0..10).forEach {
      LOGGER.info("{}% done") { it * 10 }
      delay(0.3.seconds) // yielding point - coroutines that runs this code can be SUSPENDED
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
      } // yielding point - coroutine is SUSPENDED

    LOGGER.info("This prints AFTER resuming the coroutine: $resumedComputation")
  }

  // CPS - continuation passing style
  // suspend functions compile to functions with Continuation as their last argument

  // suspend function, values (lambdas)
  val suspendLambda: suspend (Int) -> Int = { it + 1 }
  // (Int) -> Int and `suspend` (Int) -> Int are DIFFERENT TYPES

  val increment: suspend Int.() -> Int = { this + 1 }

  suspend fun demoLambda() {
    LOGGER.info("Suspend call: ${suspendLambda(2)}")
    val four = 3.increment()
    LOGGER.info("Suspend lambda with receiver call: ${increment(3)}")
  }

  // TODO: why does it not work with suspend fun main in the object?
  // @JvmStatic // public static void main(String[] args, Continuation) - what Kotlin compiles to
  // // public static void main(String[]) - for the JVM
  // suspend fun main(args: Array<String>) {
  //   takeTheBus()
  // }
}

suspend fun main(args: Array<String>) {
  // SuspendFunctions.takeTheBus()
  // SuspendFunctions.demoSuspendedCoroutine()
  SuspendFunctions.demoLambda()
}
