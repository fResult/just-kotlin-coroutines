# Just Kotlin Coroutines

This repository captures my journey through Kotlin's concurrency model — from raw JVM threads, through structured coroutines, to reactive streams with Flow and custom Actor models.\
Following the [Rock the JVM - Kotlin Coroutines & Concurrency course](https://rockthejvm.com/courses/kotlin-coroutines-and-concurrency), I worked through the full stack to understand *why* coroutines exist before reaching for them.

## Key Learning & Implementation

### JVM Concurrency & Foundations

- **Thread Mechanics & Virtual Threads:** Built up from raw `Thread` and `Runnable` to thread pools and Project Loom's Virtual Threads, demonstrating cooperative failure when threads never yield
- **Synchronization Primitives:** Fixed race conditions, deadlocks, and livelocks using `ReentrantLock` and mutual exclusion, establishing the baseline for what structured concurrency prevents

### Coroutines & Structured Concurrency

- **Suspend Functions & Builders:** Leveraged `suspend` functions and builders like `launch` and `async`/`await` to understand the continuation-passing style the Kotlin compiler generates
- **Structured Scopes & Cancellation:** Used `coroutineScope` to manage concurrent child jobs and ensure deterministic resource cleanup, making resource leaks a compile-time concern

### Reactive Streams: Channels & Flows

- **Channels & Buffering:** Handled concurrent communication with Channels, solving race conditions with, and exploring buffer overflow policies
- **Flows & UI State:** Built resilient, backpressure-aware streams using Flow operators (`map`, `scan`, `retry`), and managed read-only UI state via `MutableStateFlow`

### The Actor Model (Aktors)

- **Custom Framework Architecture:** Built a hierarchical Actor system (`ActorSystem`, `ActorContext`) from scratch, where every actor is backed by its own coroutine and unlimited-capacity `Channel`
- **Type-Safe Message Passing:** Implemented `ActorRef<T>` with a custom infix `!` operator to enforce strongly typed, asynchronous communication between actors
- **Finite-State Machine (FSM) via Behaviors:** Designed a `Behavior` system (`Setup`, `ReceiveMessage`, `Same`, `Stopped`) allowing actors to dynamically change their message-handling logic at runtime
- **State Encapsulation:** Demonstrated how both Stateful and Stateless actors eliminate shared mutable state by processing messages sequentially, removing the need for locks, and avoiding race conditions entirely

## Tech Stack

- **Kotlin** 2.2.21 (JVM toolchain 24)
- **kotlinx-coroutines** 1.10.2
- **Logback** 1.5.32 for structured logging
- **JUnit Jupiter** 6.0.3 with `kotlinx-coroutines-test`
- **Spotless** + ktlint, wired into a pre-commit Git hook (`./gradlew installGitHooks`)

## Running the Examples

Each topic lives in its own file under `src/main/kotlin/com/fResult/`.\
Files declare a `main` (or `suspend fun main`) and toggle which demo runs by un-commenting one line:

```kotlin
suspend fun main() {
  // CoroutineBuilders.startup()
  // CoroutineBuilders.globalStartup()
  CoroutineBuilders.startupValues()
}
```

Open any file in IntelliJ IDEA and click the gutter run icon next to `main`.
