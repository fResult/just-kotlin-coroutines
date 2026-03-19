package com.fResult.com.fResult.jvmConcurrency

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object ThreadsSync {
  // race condition
  var coffeeMachine = 0

  @Suppress("ktlint:standard:no-consecutive-comments")
  fun developer(index: Int) =
    Runnable {
      println("[$index] I'm a developer, I need coffee")
      val randomUpTo1Second = Random.nextLong().milliseconds.toJavaDuration()
      Thread.sleep(randomUpTo1Second)
      coffeeMachine += 1 // race condition
    /*
     * - start
     * - read coffeeMachine
     *     10,000 threads have read coffeeMachine = 0
     * - compute coffeeMachine + 1
     *     all threads have coffeeMachine + 1 = 1
     * - set coffeeMachine to that
     *     coffeeMachine set to 1
     * - end
     */
      println("[$index] I got coffee")
    }

  fun developerWithRaceCondition() {
    for (i in 1..10000) {
      Thread(developer(i)).start()
    }

    Thread.sleep(3.seconds.toJavaDuration())

    // expected 10,000, got only 4,982 (< 10,000)
    println("Coffee machine has issued $coffeeMachine cups of coffee")
  }

  @JvmStatic
  fun main(args: Array<String>) {
    developerWithRaceCondition()
  }
}
