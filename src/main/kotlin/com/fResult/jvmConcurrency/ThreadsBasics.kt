package com.fResult.com.fResult.jvmConcurrency

import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object ThreadsBasics {
  // Thread = independent unit of execution

  // Thread = data structure (maps to OS threads)
  // Runnable = piece of code to run

  val takingTheBus =
    Runnable {
      println("Getting in the bus")
      (0..10).forEach {
        println("${it * 10}% done")
        Thread.sleep(0.3.seconds.toJavaDuration())
      }
      println("Getting off the bus, I'm done")
    }

  fun runMultipleThreads() {
    val takingTheBus = Thread(takingTheBus)
    val listeningPodcast =
      thread(start = false) {
        // same as Thread(Runnable { ... })
        println("Personal development")
        Thread.sleep(2.seconds.toJavaDuration())
        println("I'm a new person now!")
      } // also starts the thread!

    // start the threads
    takingTheBus.start()
    listeningPodcast.start() // exception if we start thread multiple times

    // join threads = block until they all finish
    takingTheBus.join()
    listeningPodcast.join()
  }

  // interruption
  val scrollingSocialMedia =
    thread(start = false) {
      while (true) {
        try {
          println("Scrolling my Social Media")
          Thread.sleep(1.seconds.toJavaDuration())
        } catch (e: InterruptedException) {
          println("Oh! I scrolled too much, time to stop")
          return@thread // non-local return
        }
      }
    }

  @Suppress("ktlint:standard:max-line-length")
  fun demoInterruption() {
    scrollingSocialMedia.start()
    Thread.sleep(5.seconds.toJavaDuration())
    scrollingSocialMedia.interrupt() // throws InterruptedException on that thread = crashing the thread
    scrollingSocialMedia.join() // block forever! (unless we interrupt as above)
  }

  // executors
  fun demoExecutorsAndFutures() {
    // thread pools
    val executors = Executors.newFixedThreadPool(8)
    executors.submit {
      (1..100).forEach { n ->
        println("Counting to $n")
        Thread.sleep(100)
      }
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    // main thread
    // runThread() // this creates ANOTHER thread
    // Thread.sleep(1.seconds.toJavaDuration())
    // println("Hello from the main thread")

    // runMultipleThreads()
    // demoInterruption()
    demoExecutorsAndFutures()
  }
}
