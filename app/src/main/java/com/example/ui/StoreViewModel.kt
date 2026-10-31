package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.ContentReport
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.ProductReview
import com.example.data.model.StoreOrder
import com.example.data.model.StoreSetting
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.data.repository.StoreRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class CartState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Long = 0L
)

enum class Screen {
    HOME,
    EXPLORE,
    SUBMIT_PRODUCT,
    STORY,
    PROFILE,
    PRODUCT_DETAIL,
    SELLER_PROFILE,
    CREATE_STORY,
    STORY_VIEWER,
    CART,
    CHECKOUT,
    QRIS_PAYMENT,
    ORDER_STATUS,
    ORDER_HISTORY,
    HELP,
    SEARCH,
    CATEGORIES,
    ALL_PRODUCTS,
    OWNER_DASHBOARD,
    MODERATION_DASHBOARD,
    ADMIN_PRODUCTS,
    ADMIN_CATEGORIES,
    ADMIN_ORDERS,
    ADMIN_VERIFY_PAYMENT,
    ADMIN_SETTINGS
}

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    val repository = StoreRepository(application)
    private val auth = Firebase.auth

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _cart = MutableStateFlow(CartState())
    val cart: StateFlow<CartState> = _cart.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _selectedOrder = MutableStateFlow<StoreOrder?>(null)
    val selectedOrder: StateFlow<StoreOrder?> = _selectedOrder.asStateFlow()

    private val _selectedStory = MutableStateFlow<Story?>(null)
    val selectedStory: StateFlow<Story?> = _selectedStory.asStateFlow()

    private val _selectedSeller = MutableStateFlow<UserProfile?>(null)
    val selectedSeller: StateFlow<UserProfile?> = _selectedSeller.asStateFlow()

    private val _selectedSellerProducts = MutableStateFlow<List<Product>>(emptyList())
    val selectedSellerProducts: StateFlow<List<Product>> = _selectedSellerProducts.asStateFlow()

    private val _isFollowingSeller = MutableStateFlow(false)
    val isFollowingSeller: StateFlow<Boolean> = _isFollowingSeller.asStateFlow()

    private val _productReviews = MutableStateFlow<List<ProductReview>>(emptyList())
    val productReviews: StateFlow<List<ProductReview>> = _productReviews.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<String?>(null) // "digital", "physical", or null
    val selectedTypeFilter: StateFlow<String?> = _selectedTypeFilter.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val categories: StateFlow<List<ProductCategory>> = repository.observeCategories()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val products: StateFlow<List<Product>> = repository.observeProducts()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val storeSettings: StateFlow<StoreSetting> = repository.observeStoreSettings()
        .catch { emit(StoreSetting()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), StoreSetting())

    private val _userOrders = MutableStateFlow<List<StoreOrder>>(emptyList())
    val userOrders: StateFlow<List<StoreOrder>> = _userOrders.asStateFlow()

    private val _myProducts = MutableStateFlow<List<Product>>(emptyList())
    val myProducts: StateFlow<List<Product>> = _myProducts.asStateFlow()
    val userProducts: StateFlow<List<Product>> get() = myProducts

    private val _allProductsForModeration = MutableStateFlow<List<Product>>(emptyList())
    val allProductsForModeration: StateFlow<List<Product>> = _allProductsForModeration.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _allOrders = MutableStateFlow<List<StoreOrder>>(emptyList())
    val allOrders: StateFlow<List<StoreOrder>> = _allOrders.asStateFlow()

    val reports: StateFlow<List<ContentReport>> = repository.observeContentReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val moderationLogs: StateFlow<List<com.example.data.model.ModerationLog>> = repository.observeModerationLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _userSearchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val userSearchResults: StateFlow<List<UserProfile>> = _userSearchResults.asStateFlow()

    private val _marketplaceStats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val marketplaceStats: StateFlow<Map<String, Any>> = _marketplaceStats.asStateFlow()

    val isDeveloper: Boolean
        get() = repository.isDeveloperEmail(auth.currentUser?.email) || _userProfile.value?.isDeveloper == true || _userProfile.value?.role == "owner"

    init {
        val user = auth.currentUser
        if (user != null) {
            loadUserData(user.uid, user.displayName ?: "User", user.email ?: "")
        }
    }

    fun loadUserData(uid: String, displayName: String, email: String) {
        viewModelScope.launch {
            try {
                val profile = repository.syncUserProfile(displayName, email)
                _userProfile.value = profile
                repository.seedInitialCatalogIfEmpty()
                loadMarketplaceStats()

                launch {
                    repository.observeUserOrders(uid).collect { orders ->
                        _userOrders.value = orders
                    }
                }

                launch {
                    repository.observeUserProducts(uid).collect { prods ->
                        _myProducts.value = prods
                    }
                }

                launch {
                    repository.observeStories(uid).collect { storyList ->
                        _stories.value = storyList
                        // Update selectedStory if open
                        val curSel = _selectedStory.value
                        if (curSel != null) {
                            storyList.find { it.id == curSel.id }?.let { _selectedStory.value = it }
                        }
                    }
                }

                if (profile.role == "developer" || profile.role == "owner" || profile.role == "moderator" || isDeveloper) {
                    launch {
                        repository.observeAllOrdersForAdmin().collect { orders ->
                            _allOrders.value = orders
                        }
                    }
                    launch {
                        repository.observeAllProductsForModeration().collect { prods ->
                            _allProductsForModeration.value = prods
                        }
                    }
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error profil: ${e.message}"
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        _currentScreen.value = Screen.PRODUCT_DETAIL
        // Load reviews
        viewModelScope.launch {
            repository.observeProductReviews(product.id).collect {
                _productReviews.value = it
            }
        }
    }

    fun selectOrder(order: StoreOrder) {
        _selectedOrder.value = order
        _currentScreen.value = Screen.ORDER_STATUS
    }

    fun selectStory(story: Story) {
        _selectedStory.value = story
        _currentScreen.value = Screen.STORY_VIEWER
        val profile = _userProfile.value
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    repository.recordStoryView(story.id, uid, profile)
                } catch (_: Exception) {}
            }
        }
    }

    fun reactToStory(storyId: String, emoji: String) {
        viewModelScope.launch {
            try {
                repository.reactToStory(storyId, emoji)
            } catch (e: Exception) {
                _statusMessage.value = "Gagal kirim reaksi: ${e.message}"
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchUsersByUsername(query)
                _userSearchResults.value = results
            } catch (_: Exception) {
                _userSearchResults.value = emptyList()
            }
        }
    }

    fun clearUserSearchResults() {
        _userSearchResults.value = emptyList()
    }

    fun viewSellerProfile(sellerId: String, fallbackName: String = "Seller", fallbackAvatar: String = "") {
        viewModelScope.launch {
            try {
                val profile = repository.getUserProfile(sellerId) ?: UserProfile(
                    userId = sellerId,
                    displayName = fallbackName,
                    avatarUrl = fallbackAvatar,
                    role = "seller"
                )
                _selectedSeller.value = profile
                _currentScreen.value = Screen.SELLER_PROFILE

                launch {
                    repository.observeSellerApprovedProducts(sellerId).collect {
                        _selectedSellerProducts.value = it
                    }
                }
                launch {
                    repository.isFollowing(sellerId).collect {
                        _isFollowingSeller.value = it
                    }
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal memuat profil seller: ${e.message}"
            }
        }
    }

    fun toggleFollowSeller(targetSellerId: String) {
        viewModelScope.launch {
            try {
                val followingNow = repository.toggleFollow(targetSellerId)
                _isFollowingSeller.value = followingNow
                _statusMessage.value = if (followingNow) "Berhasil mengikuti penjual" else "Batal mengikuti"
            } catch (e: Exception) {
                _statusMessage.value = "Gagal follow: ${e.message}"
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            try {
                val isFav = repository.toggleFavorite(productId)
                _userProfile.update { current ->
                    if (current == null) null
                    else {
                        val favs = current.favorites.toMutableList()
                        if (isFav) favs.add(productId) else favs.remove(productId)
                        current.copy(favorites = favs)
                    }
                }
                _statusMessage.value = if (isFav) "Ditambahkan ke Favorit" else "Dihapus dari Favorit"
            } catch (e: Exception) {
                _statusMessage.value = "Gagal update favorit: ${e.message}"
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun setTypeFilter(type: String?) {
        _selectedTypeFilter.value = type
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // --- MARKETPLACE PRODUCT SUBMISSION ---
    fun submitProduct(product: Product, onSuccess: () -> Unit) {
        val userRole = _userProfile.value?.role ?: "customer"
        val isOwnerOrMod = userRole == "owner" || userRole == "moderator"
        viewModelScope.launch {
            try {
                repository.submitProduct(product, isOwnerOrMod = isOwnerOrMod)
                _statusMessage.value = if (isOwnerOrMod) {
                    "Produk langsung disetujui & dipublikasikan!"
                } else {
                    "Pengajuan produk berhasil dikirim! Menunggu moderasi owner/tim ZX."
                }
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "Gagal mengajukan produk: ${e.message}"
            }
        }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                _statusMessage.value = "Produk berhasil diperbarui"
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "Gagal memperbarui: ${e.message}"
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(productId)
                _statusMessage.value = "Produk berhasil dihapus"
            } catch (e: Exception) {
                _statusMessage.value = "Gagal hapus produk: ${e.message}"
            }
        }
    }

    fun moderateProduct(productId: String, status: String, reason: String = "") {
        moderateProductWithAudit(productId, status, reason, "")
    }

    fun moderateProductWithAudit(productId: String, status: String, reason: String = "", title: String = "") {
        viewModelScope.launch {
            try {
                repository.moderateProductWithAudit(productId, status, reason, title)
                val statusText = when (status) {
                    "approved" -> "Disetujui"
                    "rejected" -> "Ditolak"
                    "revision_requested" -> "Diminta Revisi"
                    "suspended" -> "Ditangguhkan"
                    else -> status
                }
                _statusMessage.value = "Status produk diperbarui: $statusText"
                loadMarketplaceStats()
            } catch (e: Exception) {
                _statusMessage.value = "Gagal moderasi produk: ${e.message}"
            }
        }
    }

    fun resolveReport(reportId: String, resolution: String, targetType: String, targetId: String) {
        viewModelScope.launch {
            try {
                repository.resolveReport(reportId, resolution, targetType, targetId)
                _statusMessage.value = "Laporan berhasil ditandai sebagai: $resolution"
            } catch (e: Exception) {
                _statusMessage.value = "Gagal memproses laporan: ${e.message}"
            }
        }
    }

    fun loadMarketplaceStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getMarketplaceStatistics()
                _marketplaceStats.value = stats
            } catch (_: Exception) {}
        }
    }

    fun refreshMarketplaceStats() {
        loadMarketplaceStats()
    }

    // --- STORY CREATION & ACTIONS ---
    fun createStory(story: Story, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.createStory(story)
                _statusMessage.value = "Story berhasil diunggah (Tayang 24 Jam)!"
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "Gagal membuat story: ${e.message}"
            }
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            try {
                repository.deleteStory(storyId)
                _statusMessage.value = "Story dihapus"
                if (_selectedStory.value?.id == storyId) {
                    _currentScreen.value = Screen.STORY
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal hapus story: ${e.message}"
            }
        }
    }

    // --- REVIEWS & REPORTS ---
    fun addProductReview(productId: String, rating: Int, comment: String) {
        val user = _userProfile.value
        val review = ProductReview(
            productId = productId,
            userId = user?.userId ?: "",
            userName = user?.displayName ?: "Pembeli",
            userAvatarUrl = user?.avatarUrl ?: "",
            rating = rating,
            comment = comment
        )
        viewModelScope.launch {
            try {
                repository.addProductReview(review)
                _statusMessage.value = "Ulasan berhasil dikirim! Terima kasih."
            } catch (e: Exception) {
                _statusMessage.value = "Gagal kirim ulasan: ${e.message}"
            }
        }
    }

    fun reportContent(targetType: String, targetId: String, title: String, reason: String) {
        val report = ContentReport(
            targetType = targetType,
            targetId = targetId,
            targetTitle = title,
            reason = reason
        )
        viewModelScope.launch {
            try {
                repository.submitReport(report)
                _statusMessage.value = "Laporan dikirim. Tim ZX akan meninjau dalam 24 jam."
            } catch (e: Exception) {
                _statusMessage.value = "Gagal kirim laporan: ${e.message}"
            }
        }
    }

    fun updateUserProfile(profile: UserProfile, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateUserProfile(profile)
                _userProfile.value = profile
                _statusMessage.value = "Profil berhasil disimpan"
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "Gagal perbarui profil: ${e.message}"
            }
        }
    }

    fun updateProfile(username: String, bio: String, avatarUrl: String, sellerQrUrl: String) {
        val current = _userProfile.value ?: return
        val updated = current.copy(
            username = username,
            bio = bio,
            avatarUrl = avatarUrl,
            sellerQrUrl = sellerQrUrl
        )
        updateUserProfile(updated) {}
    }

    fun saveProduct(product: Product, onComplete: () -> Unit = {}) {
        if (product.id.isBlank()) {
            submitProduct(product, onComplete)
        } else {
            updateProduct(product, onComplete)
        }
    }

    fun submitReview(productId: String, rating: Int, comment: String) {
        addProductReview(productId, rating, comment)
    }

    fun submitReport(targetType: String, targetId: String, reason: String, details: String = "") {
        reportContent(targetType, targetId, details.ifBlank { reason }, reason)
    }

    // --- CART ACTIONS ---
    fun addToCart(product: Product, quantity: Long = 1L) {
        _cart.update { current ->
            val existing = current.items.find { it.productId == product.id }
            val updatedItems = if (existing != null) {
                current.items.map {
                    if (it.productId == product.id) {
                        val newQty = (it.quantity + quantity).coerceAtMost(product.stock)
                        it.copy(quantity = newQty, subtotal = newQty * it.price)
                    } else it
                }
            } else {
                current.items + CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = quantity.coerceAtMost(product.stock),
                    subtotal = product.price * quantity,
                    productType = product.productType,
                    sellerId = product.sellerId,
                    sellerQrUrl = product.sellerQrUrl
                )
            }
            val newSubtotal = updatedItems.sumOf { it.subtotal }
            CartState(items = updatedItems, subtotal = newSubtotal)
        }
        _statusMessage.value = "${product.name} ditambahkan ke keranjang"
    }

    fun updateCartQuantity(productId: String, newQty: Long) {
        _cart.update { current ->
            val updatedItems = if (newQty <= 0) {
                current.items.filterNot { it.productId == productId }
            } else {
                current.items.map {
                    if (it.productId == productId) {
                        it.copy(quantity = newQty, subtotal = newQty * it.price)
                    } else it
                }
            }
            CartState(items = updatedItems, subtotal = updatedItems.sumOf { it.subtotal })
        }
    }

    fun removeFromCart(productId: String) {
        updateCartQuantity(productId, 0L)
    }

    fun clearCart() {
        _cart.value = CartState()
    }

    // --- CHECKOUT & ORDER ---
    fun checkout(customerContact: String, onSuccess: (StoreOrder) -> Unit) {
        if (_cart.value.items.isEmpty()) {
            _statusMessage.value = "Keranjang belanja kosong"
            return
        }
        if (customerContact.isBlank()) {
            _statusMessage.value = "Nomor kontak / WhatsApp wajib diisi"
            return
        }

        viewModelScope.launch {
            try {
                val order = repository.createOrder(_cart.value.items, customerContact)
                clearCart()
                _selectedOrder.value = order
                _statusMessage.value = "Pesanan ${order.orderCode} berhasil dibuat!"
                _currentScreen.value = Screen.QRIS_PAYMENT
                onSuccess(order)
            } catch (e: Exception) {
                _statusMessage.value = "Gagal checkout: ${e.message}"
            }
        }
    }

    fun submitPaymentProof(orderId: String, amount: Long, proofInfo: String, proofNote: String = "") {
        viewModelScope.launch {
            try {
                repository.submitPaymentProof(orderId, amount, proofInfo, proofNote)
                _statusMessage.value = "Bukti pembayaran terkirim! Menunggu verifikasi."
                _selectedOrder.update {
                    it?.copy(
                        paymentStatus = "proof_submitted",
                        orderStatus = "awaiting_verification",
                        proofUrl = proofInfo,
                        proofNote = proofNote
                    )
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal kirim bukti: ${e.message}"
            }
        }
    }

    // --- OWNER / MODERATOR ACTIONS ---
    fun verifyPayment(orderId: String, isApproved: Boolean, notes: String = "", rejectionReason: String = "") {
        viewModelScope.launch {
            try {
                val adminUid = repository.requireUserId()
                repository.verifyPayment(orderId, adminUid, isApproved, notes, rejectionReason)
                _statusMessage.value = if (isApproved) "Pembayaran disetujui & pesanan selesai!" else "Pembayaran ditolak."
                _selectedOrder.update {
                    it?.copy(
                        paymentStatus = if (isApproved) "paid" else "rejected",
                        orderStatus = if (isApproved) "completed" else "cancelled",
                        digitalFulfillmentNotes = notes,
                        rejectionReason = rejectionReason
                    )
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal verifikasi pembayaran: ${e.message}"
            }
        }
    }

    fun addCategory(name: String, desc: String, icon: String = "Gamepad", onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.addCategory(name, desc, icon)
                _statusMessage.value = "Kategori berhasil ditambahkan"
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "Gagal tambah kategori: ${e.message}"
            }
        }
    }

    fun updateStoreSettings(settings: StoreSetting) {
        viewModelScope.launch {
            try {
                repository.updateStoreSettings(settings)
                _statusMessage.value = "Pengaturan toko berhasil diperbarui"
            } catch (e: Exception) {
                _statusMessage.value = "Gagal perbarui pengaturan: ${e.message}"
            }
        }
    }
}
