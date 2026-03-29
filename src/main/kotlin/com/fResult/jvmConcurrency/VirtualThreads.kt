package com.fResult.com.fResult.jvmConcurrency

import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object VirtualThreads {
  // CPUs <--- (OS scheduler) OS Threads <- (1 to 1) JVM threads
  // preemptive schedule - no control over which threads are suspended at which time

  // Virtual threads - managed/scheduled by the JVM
  // CPUs <--- (OS scheduler) OS Threads <--- (JVM scheduler) virtual threads
  // OS threads - 1000s - 10000s
  // Virtual threads - millions easily (on the heap)

  fun indefinitely() {
    val threads =
      (1..1_000_000).map { i ->
        Thread.ofVirtual().start {
          while (true) {
            // do nothing
          }
        }
      }

    Thread.sleep(5.seconds.toJavaDuration())
    println("virtual threads ok")
  }

  fun demoVirtualThreadFactory() {
    val factory = Thread.ofVirtual().name("fResult-", 0).factory()
    val threads =
      (1..1_000_000).map { i ->
        factory
          .newThread {
            while (true) {
              Thread.sleep(Random.nextLong(1000))
              println("[${Thread.currentThread().name}] I'm a virtual thread")
            }
          }.start()
      }

    Thread.sleep(5.seconds.toJavaDuration())
    println("all virtual threads done")
  }

  // OS threads? as many as CPU cores you have
  fun demoVirtualExecutor() {
    // val executor = Executors.newVirtualThreadPerTaskExecutor()

    val factory = Thread.ofVirtual().name("fResult-", 0).factory()
    val executor = Executors.newThreadPerTaskExecutor(factory)
    val futures =
      (1..1_000_000).map { _ ->
        executor.submit {
          while (true) {
            Thread.sleep(Random.nextLong(1000))
            println("[${Thread.currentThread().name}] I'm a task running on a virtual thread")
          }
        }
      }

    Thread.sleep(7.seconds.toJavaDuration())
    println("all virtual threads done")
  }

  fun threadWhichNeverYields() {
  }

  @JvmStatic
  fun main(args: Array<String>) {
    // indefinitely()
    // demoVirtualThreadFactory()
    demoVirtualExecutor()
  }
}
