package com.fResult.com.fResult.coroutines

import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

@Suppress("ktlint:standard:no-consecutive-comments")
object DispatchersAndContexts {
  private val LOGGER = LoggerFactory.getLogger(javaClass)

  // dispatcher = thread pool + scheduler of coroutines
  val basicDispatcher = Dispatchers.Default

  private fun runningTaskWithNumber(n: Int) {
    LOGGER.info("Running task $n...")
  }

  suspend fun demoDispatcher() {
    val limitedDispatcher = basicDispatcher.limitedParallelism(1) // single-thread dispatcher

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    val singleThreadedDispatcher = newSingleThreadContext("TheOneThread")

    LOGGER.info("Demo limited dispatcher")
    coroutineScope {
      launch(limitedDispatcher) {
        (1..100).forEach(::runningTaskWithNumber)
      }

      launch(limitedDispatcher) {
        LOGGER.info("The first tasks must end...")
      }

      launch(limitedDispatcher) {
        (201..300).forEach(::runningTaskWithNumber)
      }

      launch(limitedDispatcher) {
        LOGGER.info("... before the last one does.")
      }
    } // coroutines on a single threaded dispatcher will run sequentially

    LOGGER.info("Demo unconfined dispatcher")
    coroutineScope {
      launch(Dispatchers.Unconfined) {
        LOGGER.info("Unconfined - start") // will run on the calling thread
        delay(500.milliseconds) // suspension point
        LOGGER.info("Unconfined - resumed")
      }
      launch {
        LOGGER.info("Regular - start")
        delay(500.milliseconds) // suspension point
        LOGGER.info("Regular - resumed")
      }
    }
  }

  /*
   * Dispatcher types
   * - Default - thread pool between 2 and N_CORES
   *   - use this for regular coroutines
   *   - can configure `kotlinx.coroutines.scheduler.core.pool.size` as JVM argument
   *   - can configure `kotlinx.coroutines.scheduler.max.pool.size` as JVM argument
   * - IO - used for blocking actions (UI tasks)
   *   - e.g., on Android or other forms of blocking operation waiting for reading from db, for a socket
   *   - more complex design
   *   - thread pool max(N_CORES, 64)
   * - Main - for the main app, usually single-threaded
   * - Unconfined - not bound by a certain thread pool
   *   - runs a coroutine in the calling thread
   *   - suspends at the first suspension point
   *   - resumes on the thread that caused that suspension
   *   - good for starting cheap coroutines, when you don't care where they're resumed
   *     (shouldn't use it in 95% of the code)
   */

  // contexts
  val context: CoroutineContext = basicDispatcher
  val coroutineName: CoroutineContext = CoroutineName("myCoroutine")
  val combinedContext = context + coroutineName
  val nameExtracted = combinedContext[CoroutineName] // CoroutineName("myCoroutine")
  /*
   * You can think of the coroutine context is like Map
   * {
   *   CoroutineName -> CoroutineName("myCoroutine")
   *   CoroutineDispatcher -> basicDispatcher
   * }
   */

  private suspend fun developer() {
    // coroutineContext is available from all suspend functions
    val coroutineName = currentCoroutineContext()[CoroutineName]?.name ?: "unknown"
    @Suppress("ktlint:standard:max-line-length")
    LOGGER.info(
      "[dev $coroutineName] I'm a developer. I need to write code or I'll die.",
    )
    delay(Random.nextLong(1000).milliseconds)
    LOGGER.info("[dev $coroutineName] I wrote code today.")
  }

  suspend fun startup() {
    LOGGER.info("9AM, let's start")
    coroutineScope {
      launch(context = CoroutineName("Alice")) { developer() }
      launch(context = CoroutineName("Bob")) { developer() }
    }
    LOGGER.info("6PM, we'll never make it")
  }

  // contexts are inherited to child coroutines
  suspend fun startupInheritance() {
    LOGGER.info("9AM, let's start")
    coroutineScope {
      launch(context = CoroutineName("Team A")) {
        // child coroutines will inherit the Team A name
        launch { developer() }
        launch { developer() }

        // ... but may be overridden
        launch(context = CoroutineName("Team lead")) { developer() }

        // can override for multiple children
        withContext(CoroutineName("All stars")) {
          launch { developer() }
          launch { developer() }
        }
      }
    }
    LOGGER.info("6PM, we'll never make it")
  }

  /*
   * - dispatcher
   * - coroutine id
   * - coroutine name
   * - coroutine exception handler
   * - job handler
   * - your own values
   * - thread local (for interacting with Java code using ThreadLocal)
   */

  private class TeamName(
    val name: String,
  ) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<TeamName>
  }

  private suspend fun developerWithTeam() {
    // coroutineContext is available from all suspend functions
    val name = currentCoroutineContext()[CoroutineName]?.name ?: "unknown"
    val teamName = currentCoroutineContext()[TeamName]?.name ?: "unknown"
    @Suppress("ktlint:standard:max-line-length")
    LOGGER.info(
      "[dev $name] I'm a developer, working for $teamName. I need to write code or I'll die.",
    )
    delay(Random.nextLong(1000).milliseconds)
    LOGGER.info("[dev $name] I wrote code today.")
  }

  suspend fun startupComplexContext() {
    LOGGER.info("9AM, let's start")
    coroutineScope {
      launch(context = CoroutineName("Alice") + TeamName("Analytics")) { developerWithTeam() }
      launch(context = CoroutineName("Bob") + TeamName("Frontend")) { developerWithTeam() }
    }
    LOGGER.info("6PM, we'll never make it")
  }
}

suspend fun main() {
  // DispatchersAndContexts.demoDispatcher()
  // DispatchersAndContexts.startup()
  // DispatchersAndContexts.startupInheritance()
  DispatchersAndContexts.startupComplexContext()
}
