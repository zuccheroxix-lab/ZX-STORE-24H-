package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.CartItem
import com.example.data.model.PaymentProof
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.StoreOrder
import com.example.data.model.StoreSetting
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun isOwnerEmail(email: String?): Boolean {
        return email.equals("candrajouva03@gmail.com", ignoreCase = true)
    }

    // --- USER PROFILE ---
    suspend fun syncUserProfile(displayName: String, email: String): UserProfile {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val role = if (isOwnerEmail(email)) "owner" else "customer"

        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            val payload = hashMapOf(
                "userId" to uid,
                "displayName" to displayName.ifBlank { "ZX Customer" },
                "email" to email,
                "role" to role,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return UserProfile(
                userId = uid,
                displayName = displayName.ifBlank { "ZX Customer" },
                email = email,
                role = role
            )
        } else {
            val existing = snapshot.toObject(UserProfile::class.java)
            if (existing != null && isOwnerEmail(email) && existing.role != "owner") {
                docRef.update("role", "owner").await()
                return existing.copy(role = "owner")
            }
            return existing ?: UserProfile(userId = uid, displayName = displayName, email = email, role = role)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserProfile::class.java))
        }
        awaitClose { listener.remove() }
    }

    // --- CATEGORIES ---
    fun observeCategories(): Flow<List<ProductCategory>> {
        val path = "categories"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(ProductCategory::class.java).filter { it.isActive }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addCategory(name: String, description: String): String {
        requireUserId()
        val catId = "cat_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("categories").document(catId)
        val payload = hashMapOf(
            "name" to name,
            "description" to description,
            "isActive" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return catId
    }

    // --- PRODUCTS ---
    fun observeProducts(): Flow<List<Product>> {
        val path = "products"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(Product::class.java).filter { it.isActive }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeAllProductsForAdmin(): Flow<List<Product>> {
        val path = "products"
        return db.collection(path)
            .snapshots()
            .map { snap -> snap.toObjects(Product::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun saveProduct(product: Product): String {
        requireUserId()
        val prodId = if (product.id.isNotBlank()) product.id else "prod_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("products").document(prodId)
        val isNew = product.id.isBlank()

        val payload = hashMapOf<String, Any>(
            "name" to product.name,
            "description" to product.description,
            "categoryId" to product.categoryId,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "stock" to product.stock,
            "isActive" to product.isActive,
            "productType" to product.productType,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isNew) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }
        docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
        return prodId
    }

    suspend fun updateProductStock(productId: String, newStock: Long) {
        val docRef = db.collection("products").document(productId)
        docRef.update(
            mapOf(
                "stock" to newStock,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // --- ORDERS ---
    fun observeUserOrders(userId: String): Flow<List<StoreOrder>> {
        val path = "orders"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(StoreOrder::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeAllOrdersForAdmin(): Flow<List<StoreOrder>> {
        val path = "orders"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(StoreOrder::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createOrder(
        items: List<CartItem>,
        customerContact: String
    ): StoreOrder {
        val uid = requireUserId()
        val orderDocId = "order_${UUID.randomUUID().toString().take(8)}"
        val orderCode = "ZX-${System.currentTimeMillis().toString().takeLast(6)}"
        val docRef = db.collection("orders").document(orderDocId)

        val subtotal = items.sumOf { it.price * it.quantity }
        val total = subtotal

        val serializedItems = items.map {
            mapOf(
                "productId" to it.productId,
                "name" to it.name,
                "price" to it.price,
                "quantity" to it.quantity,
                "subtotal" to (it.price * it.quantity),
                "productType" to it.productType
            )
        }

        val payload = hashMapOf<String, Any>(
            "orderCode" to orderCode,
            "userId" to uid,
            "items" to serializedItems,
            "subtotal" to subtotal,
            "total" to total,
            "paymentMethod" to "QRIS",
            "paymentStatus" to "pending",
            "orderStatus" to "awaiting_payment",
            "customerContact" to customerContact,
            "digitalFulfillmentNotes" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(payload).await()

        return StoreOrder(
            id = orderDocId,
            orderCode = orderCode,
            userId = uid,
            items = serializedItems,
            subtotal = subtotal,
            total = total,
            paymentMethod = "QRIS",
            paymentStatus = "pending",
            orderStatus = "awaiting_payment",
            customerContact = customerContact
        )
    }

    suspend fun submitPaymentProof(orderId: String, amount: Long, proofUrlOrText: String) {
        val uid = requireUserId()
        val orderDocRef = db.collection("orders").document(orderId)

        // Create Payment record
        val paymentId = "pay_${UUID.randomUUID().toString().take(8)}"
        val paymentRef = db.collection("payments").document(paymentId)
        val paymentPayload = hashMapOf<String, Any>(
            "orderId" to orderId,
            "userId" to uid,
            "method" to "QRIS",
            "amount" to amount,
            "status" to "proof_submitted",
            "proofUrl" to proofUrlOrText,
            "createdAt" to FieldValue.serverTimestamp()
        )
        paymentRef.set(paymentPayload).await()

        // Update Order status
        orderDocRef.update(
            mapOf(
                "paymentStatus" to "proof_submitted",
                "orderStatus" to "awaiting_verification",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun verifyPayment(
        orderId: String,
        adminUid: String,
        isApproved: Boolean,
        fulfillmentNotes: String = ""
    ) {
        val orderDocRef = db.collection("orders").document(orderId)
        val paymentStatus = if (isApproved) "paid" else "rejected"
        val orderStatus = if (isApproved) "completed" else "cancelled"

        val updateMap = hashMapOf<String, Any>(
            "paymentStatus" to paymentStatus,
            "orderStatus" to orderStatus,
            "digitalFulfillmentNotes" to fulfillmentNotes,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        orderDocRef.update(updateMap).await()
    }

    // --- STORE SETTINGS ---
    fun observeStoreSettings(): Flow<StoreSetting> = callbackFlow {
        val docRef = db.collection("storeSettings").document("settings")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(StoreSetting::class.java) ?: StoreSetting())
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateStoreSettings(settings: StoreSetting) {
        val docRef = db.collection("storeSettings").document("settings")
        val payload = hashMapOf<String, Any>(
            "storeName" to settings.storeName,
            "logoUrl" to settings.logoUrl,
            "description" to settings.description,
            "qrisImageUrl" to settings.qrisImageUrl,
            "supportContact" to settings.supportContact,
            "storeNotice" to settings.storeNotice
        )
        docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    // --- SEED INITIAL CATALOG (If Empty) ---
    suspend fun seedInitialCatalogIfEmpty() {
        val currentProducts = db.collection("products").limit(1).get().await()
        if (currentProducts.isEmpty) {
            // Seed Categories
            val catGames = "cat_game_topup"
            val catVouchers = "cat_digital_voucher"
            val catMerch = "cat_zx_apparel"

            db.collection("categories").document(catGames).set(
                hashMapOf(
                    "name" to "Game Top Up",
                    "description" to "Direct instant top-up for popular online games",
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            db.collection("categories").document(catVouchers).set(
                hashMapOf(
                    "name" to "Digital Vouchers",
                    "description" to "Streaming, gaming platform & premium subscriptions",
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            db.collection("categories").document(catMerch).set(
                hashMapOf(
                    "name" to "ZX Merchandise",
                    "description" to "Official ZUCCHERO XANN Limited Cyber Apparel",
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Seed Products
            val sampleProducts = listOf(
                Product(
                    id = "prod_ml_500",
                    name = "Mobile Legends 500+65 Diamonds",
                    description = "Top up instan MLBB 24 Jam. Masukkan ID & Server di catatan kontak WhatsApp.",
                    categoryId = catGames,
                    price = 145000L,
                    imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80",
                    stock = 999L,
                    isActive = true,
                    productType = "digital"
                ),
                Product(
                    id = "prod_ff_720",
                    name = "Free Fire 720 Diamonds",
                    description = "Top up FF Express 24 Jam resmi via server ZX STORE.",
                    categoryId = catGames,
                    price = 98000L,
                    imageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&auto=format&fit=crop&q=80",
                    stock = 500L,
                    isActive = true,
                    productType = "digital"
                ),
                Product(
                    id = "prod_steam_100k",
                    name = "Steam Wallet IDR 120,000 Code",
                    description = "Kode voucher Steam Wallet resmi untuk membeli game & in-game item.",
                    categoryId = catVouchers,
                    price = 135000L,
                    imageUrl = "https://images.unsplash.com/photo-1612287232057-0a9c687053e1?w=500&auto=format&fit=crop&q=80",
                    stock = 45L,
                    isActive = true,
                    productType = "digital"
                ),
                Product(
                    id = "prod_spotify_3m",
                    name = "Spotify Premium Individual 3 Bulan",
                    description = "Langganan Spotify Premium tanpa iklan, kualitas audio high bitrate.",
                    categoryId = catVouchers,
                    price = 85000L,
                    imageUrl = "https://images.unsplash.com/photo-1614680376593-902f749f7ffc?w=500&auto=format&fit=crop&q=80",
                    stock = 80L,
                    isActive = true,
                    productType = "digital"
                ),
                Product(
                    id = "prod_zx_hoodie",
                    name = "ZX Cyber Electric Hoodie - Limited Edition",
                    description = "Heavyweight 330gsm cotton fleece dengan reflective electric cyan ZX emblem.",
                    categoryId = catMerch,
                    price = 389000L,
                    imageUrl = "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=500&auto=format&fit=crop&q=80",
                    stock = 25L,
                    isActive = true,
                    productType = "physical"
                )
            )

            for (p in sampleProducts) {
                saveProduct(p)
            }
        }
    }
}
