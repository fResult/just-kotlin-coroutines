package com.fResult.com.fResult.advanced

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class Product(
  val id: Int,
  val name: String,
  val price: Double,
)

object Flows {
  private val LOGGER = LoggerFactory.getLogger(javaClass)

  val products =
    listOf(
      Product(1, "laptop", 999.99),
      Product(2, "smartphone", 1999.99),
      Product(3, "tablet", 799.99),
      Product(4, "smartwatch", 399.99),
    )

  // flow = potentially infinite "list"
  val productFlow =
    flowOf(
      Product(1, "laptop", 999.99),
      Product(2, "smartphone", 1999.99),
      Product(3, "tablet", 799.99),
      Product(4, "smartwatch", 399.99),
      // emitted at a later point
    )
  val productFlowV2 = products.asFlow()

  // emit values
  private suspend fun delayProductFlow(): Flow<Product> =
    flow {
      // emit elements in this scope
      products.forEach { product ->
        emit(product)
        delay(500.milliseconds) // semantic blocking
      }
    }

  private fun Logger.infoOf(message: String): (Any) -> Unit =
    { any ->
      this.info(message, any)
    }

  suspend fun demoBuildFlow() {
    productFlow.collect(LOGGER.infoOf("Product: {}"))
    productFlowV2.collect(LOGGER.infoOf("Product: {}"))
    LOGGER.info("Wait for 3 seconds...")
    delay(3000.milliseconds)

    val productFlowV3 = buildFlow()
    productFlowV3.collect(LOGGER.infoOf("Product: {}"))
  }
}

suspend fun main() {
  Flows.demoBuildFlow()
}
