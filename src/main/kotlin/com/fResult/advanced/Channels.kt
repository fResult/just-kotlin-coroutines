@file:Suppress("ktlint:standard:no-consecutive-comments")

package com.fResult.com.fResult.advanced

import java.math.BigDecimal
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

object Channels {
  private val LOGGER = LoggerFactory.getLogger(javaClass)

  // channel = concurrent queue
  // producer-consumer problem

  @OptIn(ExperimentalTime::class)
  data class StockPrice
    constructor(
      val symbol: String,
      val price: BigDecimal,
      val timestamp: Instant,
    )

  @OptIn(ExperimentalTime::class)
  suspend fun pushStocks(channel: SendChannel<StockPrice>) {
    LOGGER.info("Trying to add an `AAPL` element...")
    channel.send(StockPrice("AAPL", BigDecimal(100), Clock.System.now()))
    LOGGER.info("Pushed an `AAPL` element")

    delay(Random.nextInt(3000).milliseconds)

    LOGGER.info("Trying to add a `GOOG` element...")
    channel.send(StockPrice("GOOG", BigDecimal(789), Clock.System.now()))
    LOGGER.info("Pushed a `GOOG` element")

    delay(Random.nextInt(3000).milliseconds)

    LOGGER.info("Trying to add an `MSFT` element...")
    channel.send(StockPrice("MSFT", BigDecimal(78), Clock.System.now()))
    LOGGER.info("Pushed an `MSFT` element")

    // when done, close the channel
    channel.close() // cannot push any new elements into the channel
    // semantic blocking + suspension point
  }

  @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
  suspend fun readStocksWithChannelClosedCheck(channel: ReceiveChannel<StockPrice>) {
    repeat(4) { idx ->
      /*
       * ⚠️ Race condition:
       * `isClosedForReceive` and `receive()` are NOT atomic.
       *
       * The channel can be open when checked but closed before `receive()` executes.
       * → `receive()` may throw (channel already closed).
       *
       * Example:
       * - B: checks `!isClosedForReceive` → true
       * - (context switch)
       * - A: closes channel
       * - B: calls `receive()` → ❌ fails
       *
       * Prefer `for (item in channel)` or `receiveCatching()`.
       */
      channel
        .takeUnless(ReceiveChannel<*>::isClosedForReceive)
        ?.receive()
        ?.also { price ->
          // the channel might be closed here
          LOGGER.info("[index: {}] I've read {}", idx, price)
          delay(500.milliseconds)
        }
      // receiving is semantically blocking + suspension point
      // receiving from a closed channel is an error
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
  suspend fun readStocksWithReceiveCaching(channel: ReceiveChannel<StockPrice>) {
    repeat(4) { idx ->
      /*
       * Can use `channel.tryReceive()`, but it is not semantic blocking (DOESN'T wait)
       */
      // LOGGER.info("[index: {}] I've read {}", idx, channel.tryReceive())
      channel
        .receiveCatching() // can be a value, failed, or closed
        .takeIf(ChannelResult<*>::isSuccess)
        ?.getOrNull()
        ?.also { maybePrice ->
          LOGGER.info("[index: {}] I've read {}", idx, maybePrice)
        }
      // receiving is semantically blocking + suspension point
      // receiving from a closed channel is an error
    }
  }

  suspend fun stockMarketTerminal() =
    coroutineScope {
      val stocksChannel = Channel<StockPrice>() // both read and write

      launch { pushStocks(stocksChannel) }
      // launch { readStocksWithChannelClosedCheck(stocksChannel) }
      launch { readStocksWithReceiveCaching(stocksChannel) }
    }

  @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
  suspend fun stockMarketNicer() =
    coroutineScope {
      val stockChannel =
        produce {
          // launches a coroutine with a `send()`
          pushStocks(channel)
        } // will automatically close the channel

      launch { readStocksWithChannelClosedCheck(stockChannel) }
    }

  /*
   * Customize a channel
   * - optional capacity
   */
  suspend fun demoCustomizeChannel() {
    coroutineScope {
      val stockChannel =
        Channel<StockPrice>(
          capacity = 2,
          // onBufferOverflow = BufferOverflow.DROP_OLDEST, // GOOG, MSFT
          onBufferOverflow = BufferOverflow.DROP_LATEST, // AAPL, GOOG
          onUndeliveredElement = { price -> LOGGER.info("{} is dropped", price.symbol) },
        ) // both read and write

      // producer
      launch {
        pushStocks(stockChannel)
        // buffer items inside
        /*
         * if the buffer is full, any `send()` will semantically block
         * - Semantically block (default)
         * - drop the oldest element in the buffer
         * - drop the element which wants to get in (latest
         */
      }

      // consumer
      launch {
        LOGGER.info("Taking a while for the consumer to start...")
        delay(5.seconds)
        readStocksWithReceiveCaching(stockChannel)
      }
    }
  }

  // closing = cannot send() any more elements, but can receive any elements CURRENTLY in the channel
  // cancelling = closing + dropping all current elements in the channel
  /*
   * `onUndeliveredElement` triggers if the channel has elements that are not about to be discarded:
   * - channel gets canceled with elements inside
   * - send() throws an error, e.g., if the channel is closed
   * - receive() throws an error, e.g., if someone cancels the coroutine calling receive()
   */
  @OptIn(ExperimentalTime::class)
  suspend fun demoOnUndelivered() =
    coroutineScope {
      val channel =
        Channel<StockPrice>(
          capacity = 10,
          onUndeliveredElement = { stockPrice -> LOGGER.info("Just dropped {}", stockPrice) },
        )

      val prices =
        listOf(
          StockPrice("AAPL", BigDecimal(100.0), Clock.System.now()),
          StockPrice("GOOG", BigDecimal(789.0), Clock.System.now()),
          StockPrice("MSFT", BigDecimal(78.0), Clock.System.now()),
          StockPrice("AMZN", BigDecimal(1234.8), Clock.System.now()),
        )

      val producer =
        launch {
          prices.forEach { price ->
            LOGGER.info("Sending: {}", price)
            channel.send(price)
            delay(200.milliseconds)
          }
        }

      val consumer =
        launch {
          repeat(2) {
            val price = channel.receive()
            LOGGER.info("Received: {}", price)
            delay(3.seconds)
          }
          channel.cancel() // close + drop all elements in the buffer
        }

      (producer to consumer).toList().joinAll()
    }
}

suspend fun main() {
  // Channels.stockMarketTerminal()
  // Channels.stockMarketNicer()
  // Channels.demoCustomizeChannel()
  Channels.demoOnUndelivered()
}
