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
  private val FRESULT_URIS_1 =
    listOf(
      "home",
      "about",
      "contact",
      "blogs?tag=oop,functional-programming",
      "blogs/tag=scala,kotlin",
    )
  private val FRESULT_URIS_2 = listOf("about", "privacy", "blogs", "products", "contact")

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
   *
   * 2. Write a function to scrape MULTIPLE websites in parallel, then combine their data.
   *    - for every website, fetch its pages
   *    - then call `scrape` for every website with its pages
   */
  private suspend fun fetchDataFromPage(pageUrl: String): String {
    delay(Random.nextLong(1000).milliseconds) // simulate network latency

    return "Data from $pageUrl"
  }

  private suspend fun fetchPageUrlsFromSite(root: String): List<String> {
    delay(Random.nextLong(1000).milliseconds)
    return FRESULT_URIS_2
  }

  private suspend fun scrape(
    siteUrl: String,
    pageUris: List<String>,
  ): String =
    coroutineScope {
      LOGGER.info("Starting scraping for $siteUrl...")

      val pageUrls = pageUris.map { pageUri -> "$siteUrl/$pageUri" }
      val pageResults =
        pageUrls
          .map { url ->
            async {
              LOGGER.info("Fetching page for {}...", url)
              return@async "\t\t" + fetchDataFromPage(url)
            }
          }.awaitAll()

      LOGGER.info("Scraping site $siteUrl complete")

      return@coroutineScope pageResults.joinToString(
        prefix = "Report for $siteUrl:\n",
        separator = "\n",
      )
    }

  private suspend fun crawl(siteUrls: List<String>): String =
    coroutineScope {
      val siteResults =
        siteUrls
          .map { siteUrl ->
            async { TODO() }
          }.awaitAll()

      siteResults.joinToString(
        prefix = "FINAL CRAWLER REPORT:\n",
        separator = "\n",
      )
    }

  suspend fun demoWebCrawler1() {
    val report = scrape("https://fResult.com", FRESULT_URIS_1)

    LOGGER.info(report)
  }
}

suspend fun main() {
  // println(StructuredConcurrency.fetchHtml("https://restcountries.com"))
  // StructuredConcurrency.demoCoroutineGroups()
  // StructuredConcurrency.demoCoroutineGroupNested()
  StructuredConcurrency.demoWebCrawler1()
}
