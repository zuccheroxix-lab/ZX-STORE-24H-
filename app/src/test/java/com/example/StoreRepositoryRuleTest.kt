package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CartItem
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun getProducts_authenticatedCustomer_returnsList(): Unit = runBlocking {
        signInTestUser(CUSTOMER_EMAIL)
        val repository = StoreRepository(firestore)

        val products = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.observeProducts().first()
        }
        assertNotNull(products)
    }

    @Test
    fun createOrder_authenticatedCustomer_createsOrderSuccessfully(): Unit = runBlocking {
        signInTestUser(CUSTOMER_EMAIL)
        val repository = StoreRepository(firestore)

        val order = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrder(
                items = listOf(
                    CartItem(
                        productId = "prod_sample",
                        name = "MLBB Diamonds",
                        price = 100000L,
                        quantity = 1L,
                        subtotal = 100000L,
                        productType = "digital"
                    )
                ),
                customerContact = "08123456789"
            )
        }
        assertTrue(order.orderCode.isNotEmpty())
        assertEquals(100000L, order.total)
        assertEquals("pending", order.paymentStatus)
    }

    @Test
    fun unauthenticatedAccess_failsWithIllegalStateException(): Unit = runBlocking {
        auth.signOut()
        val repository = StoreRepository(firestore)

        assertThrows(IllegalStateException::class.java) {
            repository.requireUserId()
        }
        Unit
    }

    private companion object {
        const val CUSTOMER_EMAIL = "customer@zxstore.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
    }
}
