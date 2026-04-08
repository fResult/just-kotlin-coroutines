package com.fResult.com.fResult.coroutines

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.slf4j.LoggerFactory

object CooperativeScheduling {
  private val LOGGER = LoggerFactory.getLogger(CooperativeScheduling::class.java)

  private suspend fun developer(idx: Int) {
    LOGGER.info("[dev $idx] I turn coffee into code")
    delay(Random.nextLong(1000).milliseconds) // suspension point
    LOGGER.info("[dev $idx] I got coffee, let's turn it into code!")
  }

  private suspend fun greedyDeveloper() {
    LOGGER.info("I want all the coffee!!")

    // no suspension point
    while (System.currentTimeMillis() % 10000 != 0L) {
      // do nothing
    } // until this loop is done, then the current OS thread is freed

    LOGGER.info("I'm done with coffee, maybe now I can code.")
  }

  private suspend fun almostGreedyDeveloper() {
    LOGGER.info("I want all the coffee!!")

    while (System.currentTimeMillis() % 10000 != 0L) {
      // delay(1.milliseconds) // suspension point
      yield() // fundamental suspension point
    }

    LOGGER.info("I'm done with coffee, maybe now I can code.")
  }

  // functions that can suspend a coroutine
  // - Cooperative Methods
  //   - yield()
  //   - delay(...)
  // - Semantically Blocking Methods
  //   - Deferred<T>#await()
  //   - Collection<Deferred<T>>#awaitAll()
  //   - Job#join()
  // - Lowest Level Suspend Functions
  //   - suspendCancellableCoroutine
  //   - suspendCoroutine
  //   - suspendCoroutineUninterceptedOrReturn

  // Lessons
  // - never run heavy coroutine CPU-bound tasks without any suspension points
  //   - like yielding, as it will starve other coroutines on the same thread

  suspend fun startup() {
    LOGGER.info("It's 9AM, let's get going")

    val singleThread = Dispatchers.Default.limitedParallelism(1)
    coroutineScope {
      launch(context = singleThread) { developer(42) }
      // launch(context = singleThread) { greedyDeveloper() }
      launch(context = singleThread) { almostGreedyDeveloper() }
      launch(context = singleThread) { developer(99) }
    }

    LOGGER.info("It's 1AM in the morning, let's go to sleep")
  }
}

suspend fun main() {
  CooperativeScheduling.startup()
}
