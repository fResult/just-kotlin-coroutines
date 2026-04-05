package com.fResult.com.fResult.coroutines

import java.net.URI
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

object StructuredConcurrency {
  private val LOGGER = LoggerFactory.getLogger(this::class.java)
  private val URLS =
    listOf(
      "https://rockthejvm.com",
      "https://coderprodigy.com",
      "https://5tobrain.com",
    )

  private suspend fun fetchHtml(url: String): String {
    LOGGER.info("Fetching page for $url...")
    delay(1.seconds)
    return URI.create(url).toURL().readText()
  }

  private suspend fun processData(data: String): String {
    println("Processing data...")
    delay(500.milliseconds)
    return "Processed: ${data.split("\n").filter(::hasText).joinToString { it.trim() }.take(100)}"
  }

  private suspend fun fetchAndProcessData(vararg urls: String): String =
    coroutineScope {
      // group of coroutines 1
      val deferredResults = urls.toList().map { url -> async { fetchHtml(url) } }

      // wait for all
      val results = deferredResults.awaitAll()

      // group of coroutine 2
      val deferredData =
        results.map { data ->
          async { "\t\t" + processData(data) }
        }

      // wait for all
      return@coroutineScope deferredData.awaitAll().joinToString(separator = "\n")
    }

  // nested coroutine scopes
  private suspend fun fetchAndProcessDataNested(vararg urls: String) =
    coroutineScope {
      // first batch of coroutines
      val htmls =
        coroutineScope {
          return@coroutineScope urls
            .map { url ->
              async { fetchHtml(url) }
            }.awaitAll()
        }

      // second batch of coroutines
      val results =
        coroutineScope {
          return@coroutineScope htmls
            .map { html ->
              async { "\t\t" + processData(html) }
            }.awaitAll()
        }

      return@coroutineScope results.joinToString(separator = "\n")
    }

  private fun hasText(str: CharSequence) = str.isNotEmpty()

  suspend fun demoCoroutineGroups() {
    LOGGER.info("Starting data fetching...")

    val result =
      fetchAndProcessData(*URLS.toTypedArray())

    LOGGER.info("Final result:\n{}", result)
  }

  suspend fun demoCoroutineGroupNested() {
    LOGGER.info("Starting data fetching (nested)...")

    val result =
      fetchAndProcessDataNested(*URLS.toTypedArray())

    LOGGER.info("Final result (nested):\n{}", result)
  }

  /*
   * ================================================ *
   * ============ Exercise - web crawler ============ *
   * ================================================ *
   *
   * 1. Implement `scrape` function which fetches all the pages for a website
   *      scrape("rockthejvm.com", ["courses/kotlin", "courses/coroutines"]
   *    - call `fetchDataFromPage` on all pages in the list in parallel
   *       - fetchDataFromPage("rockthejvm.com/courses/kotlin")
   *       - fetchDataFromPage("rockthejvm.com/courses/coroutines")
   *    - aggregate the results
   *       - "Report for rockthejvm.com: $...."
   */
  private suspend fun scrape(
    siteUrl: String,
    pageUris: List<String>,
  ): String =
    coroutineScope {
      val pageUrls = pageUris.map { pageUri -> "$siteUrl/$pageUri" }
      TODO()
    }

  suspend fun fetchDataFromPage(pageUrl: String): String {
    delay(Random.nextLong(1000).milliseconds) // simulate network latency

    return "Data from $pageUrl"
  }
}

suspend fun main() {
  // println(StructuredConcurrency.fetchHtml("https://restcountries.com"))
  // StructuredConcurrency.demoCoroutineGroups()
  StructuredConcurrency.demoCoroutineGroupNested()
}
