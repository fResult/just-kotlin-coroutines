package com.fResult.com.fResult.coroutines

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

  suspend fun startup() {
    LOGGER.info("It's 9AM, let's get going")

    val singleThread = Dispatchers.Default.limitedParallelism(1)
    coroutineScope {
      launch(context = singleThread) { developer(42) }
      launch(context = singleThread) { greedyDeveloper() }
    }

    LOGGER.info("It's 1AM in the morning, let's go to sleep")
  }
}

suspend fun main() {
  CooperativeScheduling.startup()
}
