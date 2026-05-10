package com.fResult.aktors

import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory

/*
 * - name and channel args
 * - `start()` method which pops elements off the channel and logs them
 */
internal class Actor<T>(
  private val name: String,
  private val channel: Channel<T>,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  suspend fun run() {
    while (true) {
      val message = channel.receive() // semantically blocking
      log.info("[$name]: $message")
    }
  }
}
