package com.fResult.com.fResult.coroutines

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.slf4j.LoggerFactory

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
}

suspend fun main() {
  DispatchersAndContexts.demoDispatcher()
}
