package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.StoreOrder
import com.example.data.model.StoreSetting
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OwnerDashboardScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.allOrders.collectAsState()
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Ringkasan", "Pesanan & QRIS", "Produk", "Kategori", "Pengaturan")

    // Stats calculations
    val totalSales = remember(orders) {
        orders.filter { it.paymentStatus == "paid" || it.orderStatus == "completed" }.sumOf { it.total }
    }
    val awaitingVerification = remember(orders) {
        orders.count { it.paymentStatus == "proof_submitted" || it.orderStatus == "awaiting_verification" }
    }
    val awaitingPayment = remember(orders) {
        orders.count { it.paymentStatus == "pending" }
    }
    val completedOrders = remember(orders) {
        orders.count { it.orderStatus == "completed" }
    }

    // Dialog States
    var orderToVerify by remember { mutableStateOf<StoreOrder?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.HOME) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DASHBOARD OWNER",
                    color = CyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "ZX STORE 24H",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyanPrimary.copy(alpha = 0.2f))
                    .border(1.dp, CyanPrimary, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("OWNER MODE", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SurfaceDark,
            contentColor = CyanPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = CyanPrimary
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) CyanPrimary else TextSecondary
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> OwnerOverviewTab(
                totalSales = totalSales,
                totalOrders = orders.size,
                awaitingVerification = awaitingVerification,
                awaitingPayment = awaitingPayment,
                completedOrders = completedOrders,
                activeProducts = products.size,
                recentOrders = orders.take(5),
                onVerifyClick = { orderToVerify = it },
                onViewAllOrders = { selectedTabIndex = 1 }
            )
            1 -> OwnerOrdersTab(
                orders = orders,
                onVerifyClick = { orderToVerify = it }
            )
            2 -> OwnerProductsTab(
                products = products,
                categories = categories,
                onAddProduct = { showAddProductDialog = true },
                onToggleProduct = { prod ->
                    viewModel.saveProduct(prod.copy(isActive = !prod.isActive)) {}
                }
            )
            3 -> OwnerCategoriesTab(
                categories = categories,
                onAddCategory = { showAddCategoryDialog = true }
            )
            4 -> OwnerSettingsTab(
                storeSettings = storeSettings,
                onSave = { updated -> viewModel.updateStoreSettings(updated) }
            )
        }
    }

    // Verify Payment Dialog
    if (orderToVerify != null) {
        val o = orderToVerify!!
        var fulfillmentNotes by remember { mutableStateOf(o.digitalFulfillmentNotes) }

        AlertDialog(
            onDismissRequest = { orderToVerify = null },
            containerColor = SurfaceDark,
            title = {
                Text("Verifikasi Pembayaran QRIS", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Kode: ${o.orderCode}", color = CyanAccent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Total: ${formatRupiah(o.total)}", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Kontak: ${o.customerContact}", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Catatan Pengiriman / Kode Voucher (Opsional):", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fulfillmentNotes,
                        onValueChange = { fulfillmentNotes = it },
                        placeholder = { Text("Contoh: SN: ML-98234823 Diamond Berhasil Masuk", color = TextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyPayment(o.id, isApproved = true, notes = fulfillmentNotes)
                        orderToVerify = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess, contentColor = Color(0xFF080B10))
                ) {
                    Text("Setujui (Lunas)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        viewModel.verifyPayment(o.id, isApproved = false, notes = "Pembayaran ditolak")
                        orderToVerify = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger, contentColor = Color.White)
                ) {
                    Text("Tolak")
                }
            }
        )
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priceStr by remember { mutableStateOf("") }
        var stockStr by remember { mutableStateOf("100") }
        var isDigital by remember { mutableStateOf(true) }
        var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Tambah Produk Baru", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Produk", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceElevated,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Harga (Rp)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceElevated,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("Stok", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceElevated,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isDigital) "Tipe: Digital (Instan)" else "Tipe: Fisik",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Switch(
                                checked = isDigital,
                                onCheckedChange = { isDigital = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Deskripsi", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceElevated,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceStr.toLongOrNull() ?: 0L
                        val stock = stockStr.toLongOrNull() ?: 0L
                        if (name.isNotBlank() && price > 0) {
                            viewModel.saveProduct(
                                Product(
                                    name = name,
                                    description = description,
                                    categoryId = categoryId.ifBlank { categories.firstOrNull()?.id ?: "cat_default" },
                                    price = price,
                                    stock = stock,
                                    isActive = true,
                                    productType = if (isDigital) "digital" else "physical",
                                    imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500"
                                )
                            ) {
                                showAddProductDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10))
                ) {
                    Text("Simpan Produk", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Tambah Kategori", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Nama Kategori", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = catDesc,
                        onValueChange = { catDesc = it },
                        label = { Text("Deskripsi", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.addCategory(catName, catDesc) {
                                showAddCategoryDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10))
                ) {
                    Text("Tambah", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun OwnerOverviewTab(
    totalSales: Long,
    totalOrders: Int,
    awaitingVerification: Int,
    awaitingPayment: Int,
    completedOrders: Int,
    activeProducts: Int,
    recentOrders: List<StoreOrder>,
    onVerifyClick: (StoreOrder) -> Unit,
    onViewAllOrders: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High priority verification alert
        if (awaitingVerification > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyanPrimary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$awaitingVerification Pesanan Menunggu Verifikasi QRIS!",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Periksa mutasi QRIS Anda dan setujui pembayaran pelanggan.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Penjualan",
                    value = formatRupiah(totalSales),
                    color = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Pesanan",
                    value = totalOrders.toString(),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Menunggu Verifikasi",
                    value = awaitingVerification.toString(),
                    color = if (awaitingVerification > 0) StatusWarning else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pesanan Selesai",
                    value = completedOrders.toString(),
                    color = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Orders Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pesanan Terbaru",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Lihat Semua",
                    color = CyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onViewAllOrders)
                )
            }
        }

        if (recentOrders.isEmpty()) {
            item {
                Text("Belum ada pesanan masuk.", color = TextMuted, fontSize = 13.sp)
            }
        }

        items(recentOrders) { order ->
            OrderAdminCard(
                order = order,
                onVerifyClick = { onVerifyClick(order) }
            )
        }
    }
}

@Composable
fun OwnerOrdersTab(
    orders: List<StoreOrder>,
    onVerifyClick: (StoreOrder) -> Unit
) {
    var statusFilter by remember { mutableStateOf<String?>(null) }

    val filtered = remember(orders, statusFilter) {
        if (statusFilter == null) orders
        else orders.filter { it.paymentStatus == statusFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryPill(name = "Semua (${orders.size})", isSelected = statusFilter == null, onClick = { statusFilter = null })
                }
                item {
                    CategoryPill(
                        name = "Menunggu Verifikasi (${orders.count { it.paymentStatus == "proof_submitted" }})",
                        isSelected = statusFilter == "proof_submitted",
                        onClick = { statusFilter = "proof_submitted" }
                    )
                }
                item {
                    CategoryPill(
                        name = "Menunggu Bayar (${orders.count { it.paymentStatus == "pending" }})",
                        isSelected = statusFilter == "pending",
                        onClick = { statusFilter = "pending" }
                    )
                }
                item {
                    CategoryPill(
                        name = "Lunas (${orders.count { it.paymentStatus == "paid" }})",
                        isSelected = statusFilter == "paid",
                        onClick = { statusFilter = "paid" }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada pesanan dengan filter ini", color = TextMuted)
                }
            }
        }

        items(filtered) { order ->
            OrderAdminCard(
                order = order,
                onVerifyClick = { onVerifyClick(order) }
            )
        }
    }
}

@Composable
fun OrderAdminCard(
    order: StoreOrder,
    onVerifyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderCode, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                StatusBadge(status = order.paymentStatus)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Kontak: ${order.customerContact}", color = TextSecondary, fontSize = 12.sp)
            Text("Total: ${formatRupiah(order.total)} (${order.items.size} item)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onVerifyClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = Color(0xFF080B10)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Verifikasi / Update Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun OwnerProductsTab(
    products: List<Product>,
    categories: List<ProductCategory>,
    onAddProduct: () -> Unit,
    onToggleProduct: (Product) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Katalog Produk (${products.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(
                    onClick = onAddProduct,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(products) { prod ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${formatRupiah(prod.price)} • Stok: ${prod.stock} • ${prod.productType.uppercase()}", color = CyanAccent, fontSize = 11.sp)
                    }

                    Switch(
                        checked = prod.isActive,
                        onCheckedChange = { onToggleProduct(prod) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun OwnerCategoriesTab(
    categories: List<ProductCategory>,
    onAddCategory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daftar Kategori (${categories.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(
                    onClick = onAddCategory,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(categories) { cat ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(cat.description.ifBlank { "Kategori resmi" }, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerSettingsTab(
    storeSettings: StoreSetting,
    onSave: (StoreSetting) -> Unit
) {
    var storeName by remember { mutableStateOf(storeSettings.storeName) }
    var supportContact by remember { mutableStateOf(storeSettings.supportContact) }
    var storeNotice by remember { mutableStateOf(storeSettings.storeNotice) }
    var description by remember { mutableStateOf(storeSettings.description) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Pengaturan Toko & Kontak", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Nama Toko", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = supportContact,
                onValueChange = { supportContact = it },
                label = { Text("Nomor WhatsApp Support / CS", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = storeNotice,
                onValueChange = { storeNotice = it },
                label = { Text("Pengumuman Berjalan (Marquee Banner)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi Singkat Toko", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    onSave(
                        storeSettings.copy(
                            storeName = storeName,
                            supportContact = supportContact,
                            storeNotice = storeNotice,
                            description = description
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
    }
}
