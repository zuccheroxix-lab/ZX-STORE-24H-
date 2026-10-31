package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val role: String = "customer", // "customer", "seller", "moderator", "owner"
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val favorites: List<String> = emptyList(),
    val favoriteProductIds: List<String> = emptyList(),
    val sellerQrUrl: String = "",
    val isDeveloper: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProductCategory(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconName: String = "Gamepad",
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val price: Long = 0L,
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val stock: Long = 0L,
    val isActive: Boolean = true,
    val productType: String = "digital", // "digital" or "physical"
    val deliveryInfo: String = "",
    val productUrl: String = "",
    val tags: List<String> = emptyList(),
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerAvatarUrl: String = "",
    val sellerQrUrl: String = "",
    val sellerContact: String = "",
    val moderationStatus: String = "approved", // "pending", "approved", "rejected", "revision_requested", "suspended"
    val rejectionReason: String = "",
    val moderationNote: String = "",
    val moderatedBy: String = "",
    val rating: Double = 5.0,
    val reviewCount: Long = 0L,
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
    val productType: String = "digital",
    val sellerId: String = "",
    val sellerQrUrl: String = ""
)

@IgnoreExtraProperties
data class StoreOrder(
    @DocumentId val id: String = "",
    val orderCode: String = "",
    val userId: String = "",
    val buyerName: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val subtotal: Long = 0L,
    val total: Long = 0L,
    val paymentMethod: String = "QRIS",
    val paymentStatus: String = "pending", // "pending", "proof_submitted", "paid", "rejected", "expired"
    val orderStatus: String = "awaiting_payment", // "awaiting_payment", "awaiting_verification", "processing", "completed", "cancelled"
    val customerContact: String = "",
    val sellerId: String = "",
    val sellerQrUrl: String = "",
    val proofUrl: String = "",
    val proofNote: String = "",
    val digitalFulfillmentNotes: String = "",
    val rejectionReason: String = "",
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
    val proofNote: String = "",
    val verifiedBy: String = "",
    @ServerTimestamp val verifiedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Story(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val mediaType: String = "PHOTO", // "PHOTO", "VIDEO", "TEXT", "TESTIMONIAL", "PRODUCT_LINKED"
    val mediaUrl: String = "",
    val textContent: String = "",
    val backgroundColorHex: String = "#051622",
    val caption: String = "",
    val linkedProductId: String = "",
    val linkedProductName: String = "",
    val linkedProductPrice: Long = 0L,
    val privacyMode: String = "PUBLIC", // "PUBLIC", "FOLLOWERS", "PRIVATE", "SELECTED_ACCOUNTS", "HIDE_FROM"
    val allowedUserIds: List<String> = emptyList(),
    val hiddenUserIds: List<String> = emptyList(),
    val viewersCount: Long = 0L,
    val viewers: List<String> = emptyList(),
    val viewersProfiles: List<Map<String, String>> = emptyList(),
    val reactions: Map<String, Long> = emptyMap(), // emoji -> count
    val userReactions: Map<String, String> = emptyMap(), // userId -> emoji
    @ServerTimestamp val createdAt: Timestamp? = null,
    val durationSeconds: Int = 6
)

@IgnoreExtraProperties
data class ProductReview(
    @DocumentId val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val rating: Int = 5,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ContentReport(
    @DocumentId val id: String = "",
    val targetType: String = "PRODUCT", // "PRODUCT", "STORY", "USER"
    val targetId: String = "",
    val targetTitle: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reason: String = "",
    val status: String = "pending", // "pending", "resolved", "dismissed"
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ModerationLog(
    @DocumentId val id: String = "",
    val targetType: String = "PRODUCT", // "PRODUCT", "STORY", "REPORT"
    val targetId: String = "",
    val targetTitle: String = "",
    val action: String = "", // "APPROVED", "REJECTED", "REVISION_REQUESTED", "SUSPENDED", "DELETED", "RESOLVED"
    val reason: String = "",
    val moderatorUid: String = "",
    val moderatorEmail: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoreSetting(
    @DocumentId val id: String = "settings",
    val storeName: String = "ZX STORE 24H",
    val logoUrl: String = "",
    val description: String = "ZUCCHERO XANN - 24H Premium Marketplace & Digital Community Store",
    val qrisImageUrl: String = "qris_zucchero_xann",
    val supportContact: String = "081234567890",
    val storeNotice: String = "Buka 24 Jam nonstop. Marketplace terpercaya, verifikasi cepat dan aman."
)
