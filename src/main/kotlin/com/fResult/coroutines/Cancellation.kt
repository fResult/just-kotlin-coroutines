package com.fResult.com.fResult.coroutines

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

object Cancellation {
  private val LOGGER = LoggerFactory.getLogger(this.javaClass)

  private suspend fun developer(idx: Int) {
    LOGGER.info("[dev $idx] I'm a developer, I'm working on a feature")
    while (true) {
      delay(500.milliseconds)

      // cancellable is cooperative
      // IF A COROUTINE DOESN'T HAVE A SUSPENSION POINT, IT IS ____NOT____ CANCELABLE
      // Thread.sleep(500) // This is not a coroutine suspension point, it won't be canceled

      LOGGER.info("[dev $idx] developing...")
    }
  }

  suspend fun ceo(employee: Job) {
    LOGGER.info("[ceo] I'm a CEO, I need to talk to this developer")
    delay(Random.nextLong(2000).milliseconds)

    employee.cancel() // the job will get canceled at the next suspension  point
    LOGGER.info("[ceo] I've fired the developer")

    employee.invokeOnCompletion { cause ->
      cause.takeIf { it is CancellationException }?.apply {
        LOGGER.info("The CEO terminated the employee's contract")
      }
      LOGGER.info("The developer ceased contract")
    }
  }

  suspend fun startup() {
    LOGGER.info("9AM, a beautiful day to change the world")
    coroutineScope {
      val goodDeveloperJob = launch { developer(3) }
      val lazyDeveloperJob = launch { developer(42) }

      launch { ceo(lazyDeveloperJob) }
    }
    LOGGER.info("1AM in the morning, are we still having fun?")
  }
}

suspend fun main() {
  Cancellation.startup()
}
