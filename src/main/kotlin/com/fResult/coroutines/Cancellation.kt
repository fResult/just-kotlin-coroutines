package com.fResult.com.fResult.coroutines

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
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

  // 1. handle the cancellation exception
  @Suppress("ktlint:standard:max-line-length")
  private suspend fun developerWithTry(idx: Int) {
    LOGGER.info("[dev $idx] I'm a developer, I'm working on a feature")
    while (true) {
      try {
        delay(500.milliseconds) // this point is where the coroutine gets canceled - will throw the CancellationException
        LOGGER.info("[dev $idx] developing...")
      } catch (ex: CancellationException) {
        LOGGER.info("[dev $idx] Oh no! I'm being fired!")
        // VERY IMPORTANT - continue to throw the cancellation exception
        // otherwise you'll ignore the cancellation - uncancelable
        // throw ex
        // CancellationException caught by the continuation -> will terminate the coroutine
      } finally {
        LOGGER.info("[dev $idx] I'm done with this startup")
      }
      yield()
    }
  }

  suspend fun startup() {
    LOGGER.info("9AM, a beautiful day to change the world")
    coroutineScope {
      // val goodDeveloperJob = launch { developer(3) }
      val goodDeveloperJob = launch { developerWithTry(3) }
      // val lazyDeveloperJob = launch { developer(42) }
      val lazyDeveloperJob = launch { developerWithTry(42) }

      launch { ceo(lazyDeveloperJob) }
    }
    LOGGER.info("1AM in the morning, are we still having fun?")
  }

  // 2. resources
  private class Laptop(
    val name: String,
  ) : AutoCloseable {
    init {
      LOGGER.info("Providing the laptop'$name'")
    }

    override fun close() {
      LOGGER.info("Shutting down the laptop '$name'")
    }
  }

  private suspend fun developerAtWork(idx: Int) {
    Laptop("The AVENGER").use { laptop ->
      LOGGER.info("[dev $idx] I'm a developer, I'm working on '${laptop.name}' on a feature")
      while (true) {
        delay(500.milliseconds)
        LOGGER.info("[dev $idx] developing...")
      }
    }
  }

  suspend fun startupResource() {
    LOGGER.info("9AM, a beautiful day to change the world")
    coroutineScope {
      val developerJob = launch { developerAtWork(10) }

      launch { ceo(developerJob) }
    }
    LOGGER.info("1AM in the morning, are we still having fun?")
  }

  // 3. cancelling a coroutine, cancels its children
  // cancellation propagates to children
  /*
   * launch {
   *   // coroutine 1
   *   launch {
   *     // coroutine 2, a child of coroutine 1
   *   }
   * }
   */
  @Suppress("ktlint:standard:no-consecutive-comments")
  suspend fun startupTeam() {
    LOGGER.info("9AM, a beautiful day to change the world")
    coroutineScope {
      val teamJob =
        launch {
          (1..10).forEach { n -> launch { developerAtWork(n) } }
          // if someone cancels me here, all the coroutines above will get canceled

          // I can cancel my own children
          coroutineContext.cancelChildren()

          // once a coroutine is canceled, it CANNOT create other coroutines
          delay(2.seconds)
          LOGGER.info("Trying to hack my way into the startup's budget")
          (100..110).forEach { n -> launch { developerAtWork(n) } }
        }

      launch { ceo(teamJob) }
    }
    LOGGER.info("1AM in the morning, are we still having fun?")
  }
}

suspend fun main() {
  // Cancellation.startup()
  // Cancellation.startupResource()
  Cancellation.startupTeam()
}
