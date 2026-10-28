package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "customer", // "customer" or "owner"
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProductCategory(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val price: Long = 0L,
    val imageUrl: String = "",
    val stock: Long = 0L,
    val isActive: Boolean = true,
    val productType: String = "digital", // "digital" or "physical"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: Long = 0L,
    val quantity: Long = 1L,
    val subtotal: Long = 0L,
    val productType: String = "digital"
)

@IgnoreExtraProperties
data class StoreOrder(
    @DocumentId val id: String = "",
    val orderCode: String = "",
    val userId: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val subtotal: Long = 0L,
    val total: Long = 0L,
    val paymentMethod: String = "QRIS",
    val paymentStatus: String = "pending", // "pending", "proof_submitted", "paid", "rejected", "expired"
    val orderStatus: String = "awaiting_payment", // "awaiting_payment", "awaiting_verification", "processing", "completed", "cancelled"
    val customerContact: String = "",
    val digitalFulfillmentNotes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PaymentProof(
    @DocumentId val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val method: String = "QRIS",
    val amount: Long = 0L,
    val status: String = "pending", // "pending", "proof_submitted", "paid", "rejected", "expired"
    val proofUrl: String = "",
    val verifiedBy: String = "",
    @ServerTimestamp val verifiedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoreSetting(
    @DocumentId val id: String = "settings",
    val storeName: String = "ZX STORE 24H",
    val logoUrl: String = "",
    val description: String = "ZUCCHERO XANN - 24H Premium Digital & Gaming Merchandise Store",
    val qrisImageUrl: String = "qris_zucchero_xann",
    val supportContact: String = "081234567890",
    val storeNotice: String = "Buka 24 Jam nonstop. Verifikasi QRIS cepat dan aman."
)
