package com.fResult.com.fResult.jvmConcurrency

import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object ThreadsBasic {
  // Thread = independent unit of execution

  // Thread = data structure (maps to OS threads)
  // Runnable = piece of code to run

  val takingTheBus = Runnable {
    println("Getting in the bus")
    (0..10).forEach {
      println("${it * 10}% done")
      Thread.sleep(0.3.seconds.toJavaDuration())
    }
    println("Getting off the bus, I'm done")
  }

  fun runThread() {
    val thread = Thread(takingTheBus)
    // thread is just data
    thread.start() // the code runs independently
  }

  fun runMultipleThreads() {
    val takingTheBus = Thread(takingTheBus)
    val listeningPodcast = Thread(Runnable {
      println("Personal development")
      Thread.sleep(2.seconds.toJavaDuration())
      println("I'm a new person now!")
    })
    takingTheBus.start()
    listeningPodcast.start()
  }

  @JvmStatic
  fun main(args: Array<String>) {
    // main thread
    // runThread() // this creates ANOTHER thread
    // Thread.sleep(1.seconds.toJavaDuration())
    // println("Hello from the main thread")

    runMultipleThreads()
  }
}
