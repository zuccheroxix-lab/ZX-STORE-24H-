package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.StoreOrder
import com.example.data.model.StoreSetting
import com.example.data.model.UserProfile
import com.example.data.repository.StoreRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    ALL_PRODUCTS,
    CATEGORIES,
    PRODUCT_DETAIL,
    SEARCH,
    CART,
    CHECKOUT,
    QRIS_PAYMENT,
    ORDER_STATUS,
    ORDER_HISTORY,
    PROFILE,
    HELP,
    OWNER_DASHBOARD,
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

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

    private val _allOrders = MutableStateFlow<List<StoreOrder>>(emptyList())
    val allOrders: StateFlow<List<StoreOrder>> = _allOrders.asStateFlow()

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

                launch {
                    repository.observeUserOrders(uid).collect { orders ->
                        _userOrders.value = orders
                    }
                }

                if (profile.role == "owner") {
                    launch {
                        repository.observeAllOrdersForAdmin().collect { orders ->
                            _allOrders.value = orders
                        }
                    }
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error loading profile: ${e.message}"
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        _currentScreen.value = Screen.PRODUCT_DETAIL
    }

    fun selectOrder(order: StoreOrder) {
        _selectedOrder.value = order
        _currentScreen.value = Screen.ORDER_STATUS
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
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
                    productType = product.productType
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

    fun submitPaymentProof(orderId: String, amount: Long, proofInfo: String) {
        viewModelScope.launch {
            try {
                repository.submitPaymentProof(orderId, amount, proofInfo)
                _statusMessage.value = "Bukti pembayaran terkirim! Menunggu verifikasi owner."
                // Refresh selected order
                _selectedOrder.update { it?.copy(paymentStatus = "proof_submitted", orderStatus = "awaiting_verification") }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal kirim bukti: ${e.message}"
            }
        }
    }

    // --- OWNER ACTIONS ---
    fun verifyPayment(orderId: String, isApproved: Boolean, notes: String = "") {
        viewModelScope.launch {
            try {
                val adminUid = repository.requireUserId()
                repository.verifyPayment(orderId, adminUid, isApproved, notes)
                _statusMessage.value = if (isApproved) "Pembayaran diverifikasi & disetujui!" else "Pembayaran ditolak."
                _selectedOrder.update {
                    it?.copy(
                        paymentStatus = if (isApproved) "paid" else "rejected",
                        orderStatus = if (isApproved) "completed" else "cancelled",
                        digitalFulfillmentNotes = notes
                    )
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal verifikasi pembayaran: ${e.message}"
            }
        }
    }

    fun saveProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveProduct(product)
                _statusMessage.value = "Produk berhasil disimpan"
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "Gagal simpan produk: ${e.message}"
            }
        }
    }

    fun addCategory(name: String, desc: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.addCategory(name, desc)
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
