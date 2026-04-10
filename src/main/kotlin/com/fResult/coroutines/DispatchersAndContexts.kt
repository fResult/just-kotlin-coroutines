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
}

suspend fun main() {
  DispatchersAndContexts.demoDispatcher()
}
