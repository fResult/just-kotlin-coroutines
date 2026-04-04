package com.fResult.com.fResult.coroutines

import java.net.URI
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

object StructuredConcurrency {
  private val LOGGER = LoggerFactory.getLogger(this::class.java)

  suspend fun fetchHtml(url: String): String {
    delay(1.seconds)
    return URI.create(url).toURL().readText()
  }
}

suspend fun main() {
  println(StructuredConcurrency.fetchHtml("https://restcountries.com"))
}
