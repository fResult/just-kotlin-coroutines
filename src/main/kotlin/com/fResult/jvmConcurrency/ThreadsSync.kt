package com.fResult.com.fResult.jvmConcurrency

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object ThreadsSync {
  // race condition
  var coffeeMachine = 0
  var coffeeMachineLock = ReentrantLock()

  @Suppress("ktlint:standard:no-consecutive-comments")
  fun developer(index: Int) =
    Runnable {
      println("[$index] I'm a developer, I need coffee")
      Thread.sleep(1.seconds.toJavaDuration())
      coffeeMachine += 1 // race condition
      /*
       * - start
       * - read coffeeMachine
       *     3,000 threads have read coffeeMachine = 0
       * - compute coffeeMachine + 1
       *     all threads have coffeeMachine + 1 = 1
       * - set coffeeMachine to that
       *     coffeeMachine set to 1
       * - end
       */
      println("[$index] I got coffee")
    }

  // locking
  fun syncDeveloper(index: Int) =
    Runnable {
      println("[$index] I'm a developer, I need coffee")
      Thread.sleep(1.seconds.toJavaDuration())

      // block other threads if I'm here
      coffeeMachineLock.lock()
      // thread-safe: only one thread can access this area
      coffeeMachine += 1 // SAFE to increment
      coffeeMachineLock.unlock()
      // unblock other threads waiting

      println("[$index] I got coffee")
    }

  fun developerWithRaceCondition(runnable: (Int) -> Runnable) {
    for (i in 1..3000) {
      Thread(runnable(i)).start()
    }

    Thread.sleep(3.seconds.toJavaDuration())

    // expected 3,000, got only 2,982 (< 3,000) unless you lock the race condition
    println("Coffee machine has issued $coffeeMachine cups of coffee")
  }

  fun developerAndMaintenance() {
    val developers = (1..3000).map { Thread(syncDeveloper(it)) }
    developers.forEach { it.start() }

    // maintainer
    val maintainer =
      thread {
//      Thread.sleep(900.milliseconds.toJavaDuration())
        coffeeMachineLock.lock()
        // run some maintenance
        println("Maintenance in progress. Please wait...")
        Thread.sleep(2.seconds.toJavaDuration())
        println("Maintenance complete!!")
        coffeeMachineLock.unlock() // unblocks the rest of the developer
      }

    developers.forEach { it.join() }
    maintainer.join()

    println("Coffee machine has issued $coffeeMachine cups of coffee") // 3000
  }

  // deadlock
  var userStories = 0
  var estimation = 0
  val userStoriesLock = ReentrantLock()
  val estimationLock = ReentrantLock()

  fun projectManager() =
    Thread {
      println("I'm a PM, I need an estimation to proceed with user stories")
      estimationLock.lock()
      Thread.sleep(1.seconds.toJavaDuration())
      userStoriesLock.lock()
      userStories = 4
      println("I'm the PM, user stories are completed")
      userStoriesLock.unlock()
      estimationLock.unlock()
    }

  fun developer() =
    Thread {
      println("I'm a developer, I need user stories to make an estimation")
      userStoriesLock.lock()
      Thread.sleep(1.seconds.toJavaDuration())
      estimationLock.lock()
      estimation = 15
      println("I'm the developer, estimation is done")
      userStoriesLock.unlock()
      estimationLock.unlock()
    }

  fun demoDeadlock() {
    projectManager().start()
    developer().start()
  }

  @JvmStatic
  fun main(args: Array<String>) {
    // developerWithRaceCondition(::developer)
    // developerWithRaceCondition(::syncDeveloper)
    // developerAndMaintenance()
    demoDeadlock()
  }
}
