package com.fResult.com.fResult.coroutines

import java.net.URI
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

object StructuredConcurrency {
  private val LOGGER = LoggerFactory.getLogger(this::class.java)

  suspend fun fetchHtml(url: String): String {
    LOGGER.info("Fetching page for $url...")
    delay(1.seconds)
    return URI.create(url).toURL().readText()
  }

  suspend fun processData(data: String): String {
    println("Processing data...")
    delay(500.milliseconds)
    return "Processed: ${data.split("\n").joinToString { it.trim() }.take(100)}"
  }

  suspend fun fetchAndProcessData(vararg urls: String): String =
    coroutineScope {
      // group of coroutines 1
      val deferredResults = urls.toList().map { url -> async { fetchHtml(url) } }

      // wait for all
      val results = deferredResults.awaitAll()

      // group of coroutine 2
      val deferredData =
        results.map { data ->
          async { processData(data) }
        }

      // wait for all
      return@coroutineScope deferredData.awaitAll().joinToString(separator = "\n")
    }

  suspend fun demoCoroutineGroups() {
    LOGGER.info("Starting data fetching...")

    val result =
      fetchAndProcessData(
        "https://rockthejvm.com",
        "https://coderprodigy.com",
        "https://5tobrain.com",
      )

    LOGGER.info("Final result: {}", result)
  }
}

suspend fun main() {
  // println(StructuredConcurrency.fetchHtml("https://restcountries.com"))
  StructuredConcurrency.demoCoroutineGroups()
}
