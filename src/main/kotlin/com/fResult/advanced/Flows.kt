package com.fResult.com.fResult.advanced

import java.math.BigDecimal
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
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
}

suspend fun main() {
  // Flows.demoBuildFlow()
  // Flows.demoTransformers()
  // Flows.demoFlowWithException()
  // Flows.demoMergedFlow()
  // Flows.demoConcatenatedFlow()
  Flows.demoZippedFlow()
}
