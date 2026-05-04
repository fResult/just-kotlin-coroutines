package com.fResult.com.fResult.advanced

import java.math.BigDecimal
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class Product(
  val id: Int,
  val name: String,
  val price: BigDecimal,
)

object Flows {
  private val LOGGER = LoggerFactory.getLogger(javaClass)

  val products =
    listOf(
      Product(1, "laptop", BigDecimal.valueOf(999.99)),
      Product(2, "smartphone", BigDecimal.valueOf(1999.99)),
      Product(3, "tablet", BigDecimal.valueOf(799.99)),
      Product(4, "smartwatch", BigDecimal.valueOf(399.99)),
    )

  // flow = potentially infinite "list"
  val productFlow =
    flowOf(
      Product(1, "laptop", 999.99.toBigDecimal()),
      Product(2, "smartphone", BigDecimal.valueOf(1999.99)),
      Product(3, "tablet", BigDecimal.valueOf(799.99)),
      Product(4, "smartwatch", BigDecimal.valueOf(399.99)),
      // emitted at a later point
    )
  val productFlowV2 = products.asFlow()

  // emit values
  private val delayedProductFlow =
    flow {
      // emit elements in this scope
      products.forEach { product ->
        emit(product)
        delay(500.milliseconds) // semantic blocking
      }
    }

  private fun Logger.infoOf(message: String = "{}"): (Any) -> Unit =
    { any ->
      this.info(message, any)
    }

  suspend fun demoBuildFlow() {
    productFlow.collect(LOGGER.infoOf("Product: {}"))
    productFlowV2.collect(LOGGER.infoOf("Product: {}"))
    LOGGER.info("Wait for 3 seconds...")
    delay(3000.milliseconds)

    val productFlowV3 = delayedProductFlow
    productFlowV3.collect(LOGGER.infoOf("Product: {}"))
  }

  // transformers
  // map
  private fun toUpperProductName(product: Product) = product.name.uppercase()

  private val productNameCaps = delayedProductFlow.map(::toUpperProductName)

  // filters
  private fun equalOrMoreThan500(product: Product) = product.price >= 500.toBigDecimal()

  private val filteredProducts = delayedProductFlow.filter(::equalOrMoreThan500)

  // fold - collapse the flow to a single value
  private suspend fun addProductPrice(
    acc: BigDecimal,
    product: Product,
  ) = acc + product.price

  private suspend fun totalInventoryValue() =
    delayedProductFlow.fold(BigDecimal.ZERO, ::addProductPrice)

  private val scannedValue = delayedProductFlow.scan(BigDecimal.ZERO, ::addProductPrice)

  suspend fun demoTransformers() {
    productNameCaps.collect(LOGGER.infoOf("Product Name: {}"))
    filteredProducts.collect(LOGGER.infoOf("Product: {}"))
    LOGGER.info("Total inventory value: {}", totalInventoryValue())
    scannedValue.collect(LOGGER.infoOf())
  }

  // handle exceptions
  private val productFlowWithException =
    flow {
      emit(Product(1, "laptop", BigDecimal.valueOf(999.99)))
      if (Random.nextBoolean()) {
        throw RuntimeException("Network error, cannot fetch product")
      }
      emit(Product(2, "smartphone", BigDecimal.valueOf(1999.99)))
      delay(300.milliseconds)
      emit(Product(3, "tablet", BigDecimal.valueOf(799.99)))
      emit(Product(4, "smartwatch", BigDecimal.valueOf(399.99)))
    }.retry(retries = 1) { ex ->
      ex is RuntimeException
    }.catch { ex ->
      LOGGER.warn("Caught error: {}", ex.message)
      emit(Product(0, "Unknown", BigDecimal.ZERO)) // emit a fallback product
    }

  suspend fun demoFlowWithException() {
    productFlowWithException.collect(LOGGER.infoOf("Product: {}"))
  }

  // side effects on emission
  val productFlowWithSideEffects =
    delayedProductFlow.onEach {
      LOGGER.info("generated product: {}", it)
    }

  // combine multiple flows: merging, concatenating, zipping
  val mergedProductFlow = merge(productFlow, delayedProductFlow)

  val concatenatedProductFlowV1 =
    flow {
      emitAll(productFlow)
      emitAll(filteredProducts)
    }

  val concatenatedProductFlowV2 =
    productFlow.onCompletion {
      if (it != null) {
        emitAll(filteredProducts)
      }
    }

  val orderFlow =
    flow {
      (1..4).forEach {

        delay(600.milliseconds)
        emit(it)
      }
    }

  data class Order(
    val productId: Int,
    val quantity: Int,
  )

  val zippedOrder =
    delayedProductFlow.zip(orderFlow) { product, quantity ->
      Order(product.id, quantity)
    }

  suspend fun demoMergedFlow() {
    mergedProductFlow.collect(LOGGER.infoOf("Product: {}"))
  }

  suspend fun demoConcatenatedFlow() {
    concatenatedProductFlowV1.collect(LOGGER.infoOf("Product Lambda: {}"))
    concatenatedProductFlowV2.collect(
      object : FlowCollector<Product> {
        override suspend fun emit(value: Product) {
          LOGGER.infoOf("Product SAM: {}")(value)
        }
      },
    )
  }

  suspend fun demoZippedFlow() {
    zippedOrder.collect(LOGGER.infoOf("Order: {}"))
  }

  data class TemperatureReading(
    val location: String,
    val temperature: BigDecimal,
    val timestamp: Long,
  )

  suspend fun readTemperature(): Flow<TemperatureReading> =
    flow {
      val locations = listOf("Paris", "Berlin", "Rome", "Bucharest", "Zegreb")
      while (true) {
        val location = locations.random()
        val temperature =
          (15..40).random().toLong().toBigDecimal() +
            (Random.nextInt(10).toBigDecimal() + (1 / 10).toBigDecimal())
        val timestamp = System.currentTimeMillis()

        val maybeError = abs(Random.nextInt() % 10) < 1 // 10% chance of error
        if (maybeError) {
          throw RuntimeException("Weather station error")
        }

        emit(TemperatureReading(location, temperature, timestamp))
        delay(Random.nextInt(1000).milliseconds)
      }
    }

  /*
   * Exercise: weather station
   * - transform all the temperatures to Fahrenheit (9/5 * Celsius + 32)
   * - calculate the latest average across all locations - emits all the averages
   * - catch any exception and retry the flow, 3 times max
   * - print average temperatures
   * - run the flow for 10 seconds, then cancel it
   */
  suspend fun weatherApp1() {
    val transformedFlow =
      readTemperature()
        .map { reading ->
          val fahrenheit = reading.temperature * (9 / 5).toBigDecimal() + BigDecimal.valueOf(32)
          return@map TemperatureReading(reading.location, fahrenheit, reading.timestamp)
        }.scan(BigDecimal.valueOf(0.0) to 0) { acc, reading ->
          val (sum, count) = acc
          val newSum = sum + reading.temperature
          val newCount = count + 1

          return@scan newSum to newCount
        }.map { (sum, count) ->
          return@map sum / (if (count == 0) 1 else count).toBigDecimal()
          // flow of global average temperatures
        }.onEach(LOGGER.infoOf("Average temperature: {}"))
        .retry(3) { ex ->
          LOGGER.warn("Caught error, retrying the stream...")
          return@retry ex is RuntimeException
        }.catch { _ ->
          LOGGER.warn("Caught too many errors, stopping the stream")
        }

    coroutineScope {
      val job = launch { transformedFlow.collect() }
      launch {
        delay(10.seconds)
        job.cancel()
      }
    }
  }

  /*
   * Exercise: weather station
   * - transform all the temperatures to Fahrenheit (9/5 * Celsius + 32)
   * - calculate the latest average across all locations - emits all the averages
   * - catch any exception and retry the flow, 3 times max
   * - print average temperatures
   * - run the flow for 10 seconds, then cancel it
   * - do the same thing PER LOCATION
   */
  suspend fun weatherApp2() {
    val transformedFlow =
      readTemperature()
        .map { reading ->
          val fahrenheit = reading.temperature * (9 / 5).toBigDecimal() + BigDecimal.valueOf(32)
          return@map TemperatureReading(reading.location, fahrenheit, reading.timestamp)
        }.scan(mapOf<String, Pair<BigDecimal, Int>>()) { acc, reading ->
          val (sum, count) = acc[reading.location] ?: (0.0 to 0)
          val newSum = BigDecimal.valueOf(sum.toLong()) + reading.temperature
          val newCount = count + 1

          // Map<location to (sum, count)>
          return@scan acc + (reading.location to (newSum to newCount))
        }.map { dict ->
          return@map dict.mapValues { (location, states) ->
            val (sum, count) = states
            val devider = if (count == 0) 0.0 else count
            sum.div(BigDecimal.valueOf(devider.toLong()))
          }
          // Map<location to averageTemp>
        }.onEach {
          val report =
            it.toList().joinToString(separator = "\n\t\t\t") { (location, avg) ->
              "$location: $avg  ํF"
            }
          LOGGER.info("\n\tReport:\n\t\t\t{}", report)
        }.retry(3) { ex ->
          LOGGER.warn("Caught error, retrying the stream...", ex)
          return@retry ex is RuntimeException
        }.catch { ex ->
          LOGGER.warn("Caught too many errors, stopping the stream", ex)
        }

    coroutineScope {
      val job = launch { transformedFlow.collect() }
      launch {
        delay(10.seconds)
        job.cancel()
      }
    }
  }
}

suspend fun main() {
  // Flows.demoBuildFlow()
  // Flows.demoTransformers()
  // Flows.demoFlowWithException()
  // Flows.demoMergedFlow()
  // Flows.demoConcatenatedFlow()
  // Flows.demoZippedFlow()
  // Flows.weatherApp1()
  Flows.weatherApp2()
}
