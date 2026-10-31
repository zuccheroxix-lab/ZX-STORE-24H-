package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.CartItem
import com.example.data.model.ContentReport
import com.example.data.model.PaymentProof
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.ProductReview
import com.example.data.model.StoreOrder
import com.example.data.model.StoreSetting
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

    fun isDeveloperEmail(email: String?): Boolean {
        return email.equals("candrajouva03@gmail.com", ignoreCase = true)
    }

    fun isOwnerEmail(email: String?): Boolean {
        return isDeveloperEmail(email)
    }

    fun isCurrentAccountDeveloper(): Boolean {
        return isDeveloperEmail(auth.currentUser?.email)
    }

    // --- USER PROFILE & SOCIAL ---
    suspend fun syncUserProfile(displayName: String, email: String): UserProfile {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val isDev = isDeveloperEmail(email)
        val defaultRole = if (isDev) "developer" else "customer"
        val defaultUsername = email.substringBefore("@").replace(".", "_").take(15).lowercase()

        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "displayName" to displayName.ifBlank { "ZX Customer" },
                "username" to defaultUsername,
                "email" to email,
                "avatarUrl" to (auth.currentUser?.photoUrl?.toString() ?: ""),
                "bio" to if (isDev) "⚡ Official ZX Developer & System Admin" else "Member of ZX STORE 24H Community",
                "role" to defaultRole,
                "isDeveloper" to isDev,
                "followersCount" to 0L,
                "followingCount" to 0L,
                "favorites" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return UserProfile(
                userId = uid,
                displayName = displayName.ifBlank { "ZX Customer" },
                username = defaultUsername,
                email = email,
                avatarUrl = auth.currentUser?.photoUrl?.toString() ?: "",
                bio = if (isDev) "⚡ Official ZX Developer & System Admin" else "Member of ZX STORE 24H Community",
                role = defaultRole,
                isDeveloper = isDev
            )
        } else {
            val existing = snapshot.toObject(UserProfile::class.java)
            if (existing != null && isDev && (existing.role != "developer" && existing.role != "owner" || !existing.isDeveloper)) {
                docRef.update(
                    mapOf(
                        "role" to "developer",
                        "isDeveloper" to true
                    )
                ).await()
                return existing.copy(role = "developer", isDeveloper = true)
            }
            return existing ?: UserProfile(
                userId = uid,
                displayName = displayName,
                username = defaultUsername,
                email = email,
                role = defaultRole,
                isDeveloper = isDev
            )
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "displayName" to profile.displayName,
            "username" to profile.username,
            "avatarUrl" to profile.avatarUrl,
            "bio" to profile.bio
        )
        docRef.update(payload).await()
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

    suspend fun getUserProfile(userId: String): UserProfile? {
        val doc = db.collection("users").document(userId).get().await()
        return doc.toObject(UserProfile::class.java)
    }

    suspend fun toggleFavorite(productId: String): Boolean {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snap = docRef.get().await()
        val profile = snap.toObject(UserProfile::class.java)
        val currentFavorites = profile?.favorites?.toMutableList() ?: mutableListOf()
        val isFav = currentFavorites.contains(productId)
        if (isFav) {
            currentFavorites.remove(productId)
        } else {
            currentFavorites.add(productId)
        }
        docRef.update("favorites", currentFavorites).await()
        return !isFav
    }

    suspend fun toggleFollow(targetUserId: String): Boolean {
        val uid = requireUserId()
        if (uid == targetUserId) return false
        val followDocId = "${uid}_$targetUserId"
        val followRef = db.collection("follows").document(followDocId)
        val snap = followRef.get().await()
        val targetUserRef = db.collection("users").document(targetUserId)
        val currentUserRef = db.collection("users").document(uid)

        if (snap.exists()) {
            followRef.delete().await()
            targetUserRef.update("followersCount", FieldValue.increment(-1L)).await()
            currentUserRef.update("followingCount", FieldValue.increment(-1L)).await()
            return false
        } else {
            val payload = mapOf(
                "followerId" to uid,
                "followingId" to targetUserId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            followRef.set(payload).await()
            targetUserRef.update("followersCount", FieldValue.increment(1L)).await()
            currentUserRef.update("followingCount", FieldValue.increment(1L)).await()
            return true
        }
    }

    fun isFollowing(targetUserId: String): Flow<Boolean> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(false)
            close()
            return@callbackFlow
        }
        val followDocId = "${uid}_$targetUserId"
        val followRef = db.collection("follows").document(followDocId)
        val listener = followRef.addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.exists() == true)
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

    suspend fun addCategory(name: String, desc: String, icon: String = "Gamepad"): String {
        val catId = "cat_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("categories").document(catId)
        val payload = hashMapOf<String, Any>(
            "name" to name,
            "description" to desc,
            "iconName" to icon,
            "isActive" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return catId
    }

    // --- PRODUCTS & MARKETPLACE SUBMISSION ---
    fun observeProducts(): Flow<List<Product>> {
        val path = "products"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(Product::class.java)
                    .filter { it.isActive && (it.moderationStatus.isBlank() || it.moderationStatus.equals("approved", ignoreCase = true)) }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeUserProducts(userId: String): Flow<List<Product>> {
        val path = "products"
        return db.collection(path)
            .whereEqualTo("sellerId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(Product::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeSellerApprovedProducts(sellerId: String): Flow<List<Product>> {
        val path = "products"
        return db.collection(path)
            .whereEqualTo("sellerId", sellerId)
            .snapshots()
            .map { snap ->
                snap.toObjects(Product::class.java)
                    .filter { it.isActive && it.moderationStatus.equals("approved", ignoreCase = true) }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeAllProductsForModeration(): Flow<List<Product>> {
        val path = "products"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(Product::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun submitProduct(product: Product, isOwnerOrMod: Boolean = false): String {
        val uid = requireUserId()
        val prodId = if (product.id.isBlank()) "prod_${UUID.randomUUID().toString().take(10)}" else product.id
        val docRef = db.collection("products").document(prodId)

        val sellerName = product.sellerName.ifBlank { auth.currentUser?.displayName ?: "Seller" }
        val sellerAvatar = product.sellerAvatarUrl.ifBlank { auth.currentUser?.photoUrl?.toString() ?: "" }
        val status = if (isOwnerOrMod) "approved" else "pending"

        val payload = hashMapOf<String, Any>(
            "name" to product.name,
            "description" to product.description,
            "categoryId" to product.categoryId,
            "categoryName" to product.categoryName,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "images" to (if (product.images.isNotEmpty()) product.images else listOf(product.imageUrl).filter { it.isNotBlank() }),
            "stock" to product.stock,
            "isActive" to product.isActive,
            "productType" to product.productType,
            "deliveryInfo" to product.deliveryInfo,
            "productUrl" to product.productUrl,
            "tags" to product.tags,
            "sellerId" to uid,
            "sellerName" to sellerName,
            "sellerAvatarUrl" to sellerAvatar,
            "sellerQrUrl" to product.sellerQrUrl,
            "sellerContact" to product.sellerContact,
            "moderationStatus" to status,
            "rejectionReason" to "",
            "rating" to 5.0,
            "reviewCount" to 0L,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (product.id.isBlank()) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }
        docRef.set(payload, SetOptions.merge()).await()
        return prodId
    }

    suspend fun saveProduct(product: Product): String {
        return submitProduct(product, isOwnerOrMod = true)
    }

    suspend fun updateProduct(product: Product) {
        val docRef = db.collection("products").document(product.id)
        val payload = hashMapOf<String, Any>(
            "name" to product.name,
            "description" to product.description,
            "categoryId" to product.categoryId,
            "categoryName" to product.categoryName,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "images" to product.images,
            "stock" to product.stock,
            "isActive" to product.isActive,
            "productType" to product.productType,
            "deliveryInfo" to product.deliveryInfo,
            "productUrl" to product.productUrl,
            "tags" to product.tags,
            "sellerQrUrl" to product.sellerQrUrl,
            "sellerContact" to product.sellerContact,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(payload).await()
    }

    suspend fun moderateProduct(
        productId: String,
        status: String,
        reason: String,
        moderatorId: String
    ) {
        val docRef = db.collection("products").document(productId)
        val updateMap = mapOf(
            "moderationStatus" to status,
            "rejectionReason" to reason,
            "moderatedBy" to moderatorId,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(updateMap).await()
    }

    suspend fun deleteProduct(productId: String) {
        db.collection("products").document(productId).delete().await()
    }

    // --- STORIES (24H SOCIAL SYSTEM) ---
    fun observeStories(currentUserId: String): Flow<List<Story>> {
        val path = "stories"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(Story::class.java).filter { story ->
                    // Enforce Per-Account Story Privacy
                    when (story.privacyMode.uppercase()) {
                        "PUBLIC" -> true
                        "FOLLOWERS" -> true
                        "PRIVATE" -> story.authorId == currentUserId
                        "SELECTED_ACCOUNTS" -> story.authorId == currentUserId || story.allowedUserIds.contains(currentUserId)
                        "HIDE_FROM" -> story.authorId == currentUserId || !story.hiddenUserIds.contains(currentUserId)
                        else -> true
                    }
                }.sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createStory(story: Story): String {
        val uid = requireUserId()
        val storyId = "story_${UUID.randomUUID().toString().take(10)}"
        val docRef = db.collection("stories").document(storyId)

        val payload = hashMapOf<String, Any>(
            "authorId" to uid,
            "authorName" to (story.authorName.ifBlank { auth.currentUser?.displayName ?: "ZX User" }),
            "authorUsername" to story.authorUsername,
            "authorAvatarUrl" to (story.authorAvatarUrl.ifBlank { auth.currentUser?.photoUrl?.toString() ?: "" }),
            "mediaType" to story.mediaType,
            "mediaUrl" to story.mediaUrl,
            "textContent" to story.textContent,
            "backgroundColorHex" to story.backgroundColorHex,
            "caption" to story.caption,
            "linkedProductId" to story.linkedProductId,
            "linkedProductName" to story.linkedProductName,
            "linkedProductPrice" to story.linkedProductPrice,
            "privacyMode" to story.privacyMode,
            "allowedUserIds" to story.allowedUserIds,
            "hiddenUserIds" to story.hiddenUserIds,
            "viewersCount" to 0L,
            "viewers" to emptyList<String>(),
            "durationSeconds" to 6,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return storyId
    }

    suspend fun deleteStory(storyId: String) {
        db.collection("stories").document(storyId).delete().await()
    }

    suspend fun recordStoryView(storyId: String, currentUserId: String, viewerProfile: UserProfile? = null) {
        val docRef = db.collection("stories").document(storyId)
        val profileEntry = if (viewerProfile != null) {
            mapOf(
                "userId" to currentUserId,
                "displayName" to (viewerProfile.displayName.ifBlank { "User ZX" }),
                "username" to viewerProfile.username,
                "avatarUrl" to viewerProfile.avatarUrl,
                "viewedAt" to System.currentTimeMillis().toString()
            )
        } else {
            mapOf(
                "userId" to currentUserId,
                "displayName" to (auth.currentUser?.displayName ?: "User ZX"),
                "username" to (auth.currentUser?.email?.substringBefore("@") ?: "user"),
                "avatarUrl" to (auth.currentUser?.photoUrl?.toString() ?: ""),
                "viewedAt" to System.currentTimeMillis().toString()
            )
        }

        docRef.update(
            "viewers", FieldValue.arrayUnion(currentUserId),
            "viewersProfiles", FieldValue.arrayUnion(profileEntry),
            "viewersCount", FieldValue.increment(1L)
        ).await()
    }

    suspend fun reactToStory(storyId: String, emoji: String) {
        val uid = requireUserId()
        val docRef = db.collection("stories").document(storyId)
        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            val currentStory = snap.toObject(Story::class.java) ?: return@runTransaction
            val currentEmoji = currentStory.userReactions[uid]
            val updatedReactions = currentStory.reactions.toMutableMap()
            val updatedUserReactions = currentStory.userReactions.toMutableMap()

            if (currentEmoji != null) {
                val prevCount = (updatedReactions[currentEmoji] ?: 1L) - 1L
                if (prevCount <= 0L) updatedReactions.remove(currentEmoji) else updatedReactions[currentEmoji] = prevCount
            }

            if (currentEmoji != emoji) {
                val newCount = (updatedReactions[emoji] ?: 0L) + 1L
                updatedReactions[emoji] = newCount
                updatedUserReactions[uid] = emoji
            } else {
                updatedUserReactions.remove(uid)
            }

            tx.update(
                docRef,
                mapOf(
                    "reactions" to updatedReactions,
                    "userReactions" to updatedUserReactions
                )
            )
        }.await()
    }

    suspend fun searchUsersByUsername(query: String): List<UserProfile> {
        val clean = query.trim().removePrefix("@").lowercase()
        if (clean.isBlank()) return emptyList()
        val snap = db.collection("users")
            .orderBy("username")
            .startAt(clean)
            .endAt(clean + "\uf8ff")
            .limit(10)
            .get()
            .await()
        return snap.toObjects(UserProfile::class.java)
    }

    // --- REVIEWS & RATINGS ---
    fun observeProductReviews(productId: String): Flow<List<ProductReview>> {
        val path = "reviews"
        return db.collection(path)
            .whereEqualTo("productId", productId)
            .snapshots()
            .map { snap ->
                snap.toObjects(ProductReview::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addProductReview(review: ProductReview) {
        val uid = requireUserId()
        val revId = "rev_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("reviews").document(revId)
        val payload = hashMapOf<String, Any>(
            "productId" to review.productId,
            "userId" to uid,
            "userName" to (review.userName.ifBlank { auth.currentUser?.displayName ?: "Buyer" }),
            "userAvatarUrl" to (review.userAvatarUrl.ifBlank { auth.currentUser?.photoUrl?.toString() ?: "" }),
            "rating" to review.rating,
            "comment" to review.comment,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()

        // Update product rating summary
        val prodRef = db.collection("products").document(review.productId)
        prodRef.update("reviewCount", FieldValue.increment(1L)).await()
    }

    // --- REPORTS & MODERATION AUDIT ---
    suspend fun submitReport(report: ContentReport) {
        val uid = requireUserId()
        val reportId = "rep_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("reports").document(reportId)
        val payload = hashMapOf<String, Any>(
            "targetType" to report.targetType,
            "targetId" to report.targetId,
            "targetTitle" to report.targetTitle,
            "reporterId" to uid,
            "reporterName" to (auth.currentUser?.displayName ?: "User"),
            "reason" to report.reason,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    fun observeContentReports(): Flow<List<ContentReport>> {
        val path = "reports"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(ContentReport::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun resolveReport(reportId: String, resolution: String, targetType: String, targetId: String) {
        db.collection("reports").document(reportId).update(
            mapOf(
                "status" to resolution,
                "resolvedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        logModerationAction(
            targetType = targetType,
            targetId = targetId,
            targetTitle = "Laporan #$reportId",
            action = resolution.uppercase(),
            reason = "Laporan diselesaikan oleh developer"
        )
    }

    fun observeModerationLogs(): Flow<List<com.example.data.model.ModerationLog>> {
        val path = "moderation_logs"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(com.example.data.model.ModerationLog::class.java)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun logModerationAction(
        targetType: String,
        targetId: String,
        targetTitle: String,
        action: String,
        reason: String
    ) {
        val user = auth.currentUser
        val logId = "mod_${UUID.randomUUID().toString().take(10)}"
        val payload = hashMapOf<String, Any>(
            "targetType" to targetType,
            "targetId" to targetId,
            "targetTitle" to targetTitle,
            "action" to action,
            "reason" to reason,
            "moderatorUid" to (user?.uid ?: ""),
            "moderatorEmail" to (user?.email ?: ""),
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("moderation_logs").document(logId).set(payload).await()
    }

    suspend fun moderateProductWithAudit(
        productId: String,
        status: String,
        reason: String,
        productTitle: String = ""
    ) {
        moderateProduct(productId, status, reason, auth.currentUser?.uid ?: "")
        logModerationAction(
            targetType = "PRODUCT",
            targetId = productId,
            targetTitle = productTitle,
            action = status.uppercase(),
            reason = reason
        )
    }

    suspend fun getMarketplaceStatistics(): Map<String, Any> {
        val productsSnap = db.collection("products").get().await()
        val prods = productsSnap.toObjects(Product::class.java)

        val approvedCount = prods.count { it.moderationStatus == "approved" }
        val pendingCount = prods.count { it.moderationStatus == "pending" }
        val rejectedCount = prods.count { it.moderationStatus in listOf("rejected", "revision_requested", "suspended") }
        val totalSellers = prods.map { it.sellerId }.filter { it.isNotBlank() }.distinct().count()

        val ordersSnap = db.collection("orders").get().await()
        val orders = ordersSnap.toObjects(StoreOrder::class.java)
        val paidOrders = orders.filter { it.paymentStatus == "paid" || it.orderStatus == "completed" }
        val totalGmv = paidOrders.sumOf { it.total }

        val usersCountSnap = db.collection("users").get().await()
        val totalUsers = usersCountSnap.size()

        val storiesSnap = db.collection("stories").get().await()
        val totalStories = storiesSnap.size()

        return mapOf(
            "approvedProducts" to approvedCount,
            "pendingProducts" to pendingCount,
            "rejectedProducts" to rejectedCount,
            "totalSellers" to totalSellers,
            "totalOrders" to orders.size,
            "paidOrders" to paidOrders.size,
            "totalGmv" to totalGmv,
            "totalUsers" to totalUsers,
            "totalStories" to totalStories
        )
    }

    // --- ORDERS & PAYMENTS ---
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

        val firstItem = items.firstOrNull()
        val sellerId = firstItem?.sellerId ?: ""
        val sellerQrUrl = firstItem?.sellerQrUrl ?: ""

        val serializedItems = items.map {
            mapOf(
                "productId" to it.productId,
                "name" to it.name,
                "price" to it.price,
                "quantity" to it.quantity,
                "subtotal" to (it.price * it.quantity),
                "productType" to it.productType,
                "sellerId" to it.sellerId,
                "sellerQrUrl" to it.sellerQrUrl
            )
        }

        val buyerName = auth.currentUser?.displayName ?: "Customer"

        val payload = hashMapOf<String, Any>(
            "orderCode" to orderCode,
            "userId" to uid,
            "buyerName" to buyerName,
            "items" to serializedItems,
            "subtotal" to subtotal,
            "total" to total,
            "paymentMethod" to "QRIS",
            "paymentStatus" to "pending",
            "orderStatus" to "awaiting_payment",
            "customerContact" to customerContact,
            "sellerId" to sellerId,
            "sellerQrUrl" to sellerQrUrl,
            "proofUrl" to "",
            "proofNote" to "",
            "digitalFulfillmentNotes" to "",
            "rejectionReason" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(payload).await()

        return StoreOrder(
            id = orderDocId,
            orderCode = orderCode,
            userId = uid,
            buyerName = buyerName,
            items = serializedItems,
            subtotal = subtotal,
            total = total,
            paymentMethod = "QRIS",
            paymentStatus = "pending",
            orderStatus = "awaiting_payment",
            customerContact = customerContact,
            sellerId = sellerId,
            sellerQrUrl = sellerQrUrl
        )
    }

    suspend fun submitPaymentProof(
        orderId: String,
        amount: Long,
        proofUrlOrText: String,
        proofNote: String = ""
    ) {
        val uid = requireUserId()
        val orderDocRef = db.collection("orders").document(orderId)

        val paymentId = "pay_${UUID.randomUUID().toString().take(8)}"
        val paymentRef = db.collection("payments").document(paymentId)
        val paymentPayload = hashMapOf<String, Any>(
            "orderId" to orderId,
            "userId" to uid,
            "method" to "QRIS",
            "amount" to amount,
            "status" to "proof_submitted",
            "proofUrl" to proofUrlOrText,
            "proofNote" to proofNote,
            "createdAt" to FieldValue.serverTimestamp()
        )
        paymentRef.set(paymentPayload).await()

        orderDocRef.update(
            mapOf(
                "paymentStatus" to "proof_submitted",
                "orderStatus" to "awaiting_verification",
                "proofUrl" to proofUrlOrText,
                "proofNote" to proofNote,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun verifyPayment(
        orderId: String,
        adminUid: String,
        isApproved: Boolean,
        fulfillmentNotes: String = "",
        rejectionReason: String = ""
    ) {
        val orderDocRef = db.collection("orders").document(orderId)
        val paymentStatus = if (isApproved) "paid" else "rejected"
        val orderStatus = if (isApproved) "completed" else "cancelled"

        val updateMap = hashMapOf<String, Any>(
            "paymentStatus" to paymentStatus,
            "orderStatus" to orderStatus,
            "digitalFulfillmentNotes" to fulfillmentNotes,
            "rejectionReason" to rejectionReason,
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
        docRef.set(payload, SetOptions.merge()).await()
    }

    // --- SEED INITIAL CATALOG ---
    suspend fun seedInitialCatalogIfEmpty() {
        val currentProducts = db.collection("products").limit(1).get().await()
        if (currentProducts.isEmpty) {
            val catGames = "cat_game_topup"
            val catVouchers = "cat_digital_voucher"
            val catMerch = "cat_zx_apparel"

            db.collection("categories").document(catGames).set(
                hashMapOf(
                    "name" to "Game Top Up",
                    "description" to "Direct instant top-up for popular online games",
                    "iconName" to "Gamepad",
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            db.collection("categories").document(catVouchers).set(
                hashMapOf(
                    "name" to "Digital Vouchers",
                    "description" to "Streaming, gaming platform & premium subscriptions",
                    "iconName" to "LocalOffer",
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            db.collection("categories").document(catMerch).set(
                hashMapOf(
                    "name" to "ZX Merchandise",
                    "description" to "Official ZUCCHERO XANN Limited Cyber Apparel",
                    "iconName" to "ShoppingBag",
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val sampleProducts = listOf(
                Product(
                    id = "prod_ml_500",
                    name = "Mobile Legends 500+65 Diamonds",
                    description = "Top up instan MLBB 24 Jam. Masukkan ID & Server di kontak WhatsApp.",
                    categoryId = catGames,
                    categoryName = "Game Top Up",
                    price = 145000L,
                    imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80",
                    images = listOf("https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80"),
                    stock = 999L,
                    isActive = true,
                    productType = "digital",
                    deliveryInfo = "Proses kilat otomatis 1-5 menit via sistem ZX",
                    tags = listOf("MLBB", "TopUp", "Instant"),
                    sellerName = "ZX Official",
                    moderationStatus = "approved",
                    rating = 4.9,
                    reviewCount = 142L
                ),
                Product(
                    id = "prod_ff_720",
                    name = "Free Fire 720 Diamonds",
                    description = "Top up FF Express 24 Jam resmi via server ZX STORE.",
                    categoryId = catGames,
                    categoryName = "Game Top Up",
                    price = 98000L,
                    imageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&auto=format&fit=crop&q=80",
                    images = listOf("https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&auto=format&fit=crop&q=80"),
                    stock = 500L,
                    isActive = true,
                    productType = "digital",
                    deliveryInfo = "Instan via Player ID 24H",
                    tags = listOf("FreeFire", "Fast"),
                    sellerName = "ZX Official",
                    moderationStatus = "approved",
                    rating = 4.8,
                    reviewCount = 89L
                ),
                Product(
                    id = "prod_steam_100k",
                    name = "Steam Wallet IDR 120,000 Code",
                    description = "Kode voucher Steam Wallet resmi untuk membeli game & in-game item.",
                    categoryId = catVouchers,
                    categoryName = "Digital Vouchers",
                    price = 135000L,
                    imageUrl = "https://images.unsplash.com/photo-1612287232057-0a9c687053e1?w=500&auto=format&fit=crop&q=80",
                    images = listOf("https://images.unsplash.com/photo-1612287232057-0a9c687053e1?w=500&auto=format&fit=crop&q=80"),
                    stock = 45L,
                    isActive = true,
                    productType = "digital",
                    deliveryInfo = "Kode serial dikirimkan langsung ke kontak pembeli",
                    tags = listOf("Steam", "Wallet", "Gaming"),
                    sellerName = "ZX Official",
                    moderationStatus = "approved",
                    rating = 5.0,
                    reviewCount = 37L
                ),
                Product(
                    id = "prod_spotify_3m",
                    name = "Spotify Premium Individual 3 Bulan",
                    description = "Langganan Spotify Premium tanpa iklan, kualitas audio high bitrate.",
                    categoryId = catVouchers,
                    categoryName = "Digital Vouchers",
                    price = 85000L,
                    imageUrl = "https://images.unsplash.com/photo-1614680376593-902f749f7ffc?w=500&auto=format&fit=crop&q=80",
                    images = listOf("https://images.unsplash.com/photo-1614680376593-902f749f7ffc?w=500&auto=format&fit=crop&q=80"),
                    stock = 80L,
                    isActive = true,
                    productType = "digital",
                    deliveryInfo = "Aktivasi instan via link invite / redeem code",
                    tags = listOf("Music", "Premium"),
                    sellerName = "ZX Official",
                    moderationStatus = "approved",
                    rating = 4.9,
                    reviewCount = 54L
                ),
                Product(
                    id = "prod_zx_hoodie",
                    name = "ZX Cyber Electric Hoodie - Limited Edition",
                    description = "Heavyweight 330gsm cotton fleece dengan reflective electric cyan ZX emblem.",
                    categoryId = catMerch,
                    categoryName = "ZX Merchandise",
                    price = 389000L,
                    imageUrl = "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=500&auto=format&fit=crop&q=80",
                    images = listOf("https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=500&auto=format&fit=crop&q=80"),
                    stock = 25L,
                    isActive = true,
                    productType = "physical",
                    deliveryInfo = "Dikirim via JNE / J&T Express dari Warehouse Jakarta",
                    tags = listOf("Fashion", "Merchandise", "Limited"),
                    sellerName = "ZX Official",
                    moderationStatus = "approved",
                    rating = 5.0,
                    reviewCount = 18L
                )
            )

            for (p in sampleProducts) {
                saveProduct(p)
            }
        }
    }
}
