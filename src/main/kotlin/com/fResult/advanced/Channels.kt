package com.fResult.com.fResult.advanced

import java.math.BigDecimal
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
    channel.send(StockPrice("AAPL", BigDecimal(100), Clock.System.now()))
    delay(Random.nextInt(1000).milliseconds)
    channel.send(StockPrice("GOOG", BigDecimal(789), Clock.System.now()))
    delay(Random.nextInt(1000).milliseconds)
    channel.send(StockPrice("MSFT", BigDecimal(78), Clock.System.now()))

    // when done, close the channel
    channel.close()
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
  suspend fun readStocksWithTryReceive(channel: ReceiveChannel<StockPrice>) {
    repeat(4) { idx ->
      /*
       * Can use `channel.tryReceive()`, but it is not semantic blocking (DOESN'T wait)
       */
      @Suppress("standard:no-consecutive-comments")
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
      val stocksChannel = Channel<StockPrice>()
      launch { pushStocks(stocksChannel) }
      // launch { readStocksWithChannelClosedCheck(stocksChannel) }
      launch { readStocksWithTryReceive(stocksChannel) }
    }

  @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
  suspend fun stockMarketNicer() =
    coroutineScope {
      val stockChannel =
        produce {
          // launches a coroutine with a `send()`
          channel.send(StockPrice("AAPL", BigDecimal(100), Clock.System.now()))
          delay(Random.nextInt(1000).milliseconds)
          channel.send(StockPrice("GOOG", BigDecimal(789), Clock.System.now()))
          delay(Random.nextInt(1000).milliseconds)
          channel.send(StockPrice("MSFT", BigDecimal(78), Clock.System.now()))
        } // will automatically close the channel

      launch { readStocksWithChannelClosedCheck(stockChannel) }
    }
}

suspend fun main() {
  // Channels.stockMarketTerminal()
  Channels.stockMarketNicer()
}
