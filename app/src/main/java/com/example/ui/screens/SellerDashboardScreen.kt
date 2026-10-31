package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.data.model.StoreOrder
import com.example.data.model.StorePromo
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.RoleBadge
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
fun SellerDashboardScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val myProducts by viewModel.userProducts.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val userOrders by viewModel.userOrders.collectAsState()
    val promos by viewModel.promos.collectAsState()
    val myRegistration by viewModel.mySellerRegistration.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Produk Saya", "Pesanan Masuk", "Kelola Promo", "Informasi Toko", "Statistik Penjualan")

    // Filter orders belonging to this seller
    val currentUid = userProfile?.userId ?: ""
    val sellerOrders = remember(allOrders, userOrders, currentUid) {
        val combined = (allOrders + userOrders).distinctBy { it.id }
        combined.filter { it.sellerId == currentUid }
    }

    val sellerPromos = remember(promos, currentUid) {
        promos.filter { it.sellerId == currentUid }
    }

    // Modal states
    var showCreatePromoDialog by remember { mutableStateOf(false) }
    var promoTitle by remember { mutableStateOf("") }
    var promoDiscountPercent by remember { mutableStateOf("15") }
    var promoCode by remember { mutableStateOf("") }
    var promoDesc by remember { mutableStateOf("") }

    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editPrice by remember { mutableStateOf("") }
    var editStock by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Toko & Navigasi
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.PROFILE) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = CyanPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userProfile?.shopName?.ifBlank { "DASHBOARD SELLER" } ?: "DASHBOARD SELLER",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${userProfile?.shopUsername?.ifBlank { userProfile?.username ?: "seller" }}",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        RoleBadge(role = "seller", sellerStatus = "approved")
                    }
                }
            }
        }

        // 2. Overview Card (Kode Penjualan & Real Sales KPI)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "KODE PENJUALAN RESMI", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = userProfile?.salesCode?.ifBlank { myRegistration?.salesCode ?: "ZXSELL-VERIFIED" } ?: "ZXSELL-VERIFIED",
                                color = CyanAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f))
                                .border(1.dp, CyanPrimary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("STATUS: AKTIF", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${myProducts.size}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Total Produk", color = TextSecondary, fontSize = 11.sp)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(SurfaceBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${sellerOrders.size}", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Pesanan Masuk", color = TextSecondary, fontSize = 11.sp)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(SurfaceBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val omset = sellerOrders.filter { it.paymentStatus == "paid" }.sumOf { it.total }
                            Text(text = formatRupiah(omset), color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Omset Lunas", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 3. Tab Navigation
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CyanPrimary,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyanPrimary
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) CyanPrimary else TextSecondary,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }

        // 4. Tab Content
        when (selectedTab) {
            0 -> {
                // TAB PRODUK SAYA
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Daftar Produk (${myProducts.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = { viewModel.navigateTo(Screen.SUBMIT_PRODUCT) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Produk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (myProducts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Inventory, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Belum ada produk di toko Anda", color = TextSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(Screen.SUBMIT_PRODUCT) },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                                ) {
                                    Text("Mulai Jual Produk Pertama (+)", color = CyanPrimary)
                                }
                            }
                        }
                    }
                } else {
                    items(myProducts) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = product.imageUrl.ifBlank { "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80" },
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = product.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                    Text(text = formatRupiah(product.price), color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Stok: ${product.stock}", color = TextSecondary, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusBadge(status = product.moderationStatus)
                                    }
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            editingProduct = product
                                            editPrice = product.price.toString()
                                            editStock = product.stock.toString()
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteProduct(product.id) }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = StatusDanger, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB PESANAN MASUK
                item {
                    Text(text = "Pesanan Masuk Toko (${sellerOrders.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (sellerOrders.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Belum ada pesanan masuk untuk toko Anda", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(sellerOrders) { order ->
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
                                    Text(text = order.orderCode, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    StatusBadge(status = order.paymentStatus)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Pembeli: ${order.buyerName} (${order.customerContact})", color = TextSecondary, fontSize = 11.sp)
                                Text(text = "Total: ${formatRupiah(order.total)}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.selectOrder(order) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Lihat Detail & Bukti Pembayaran", color = CyanPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB KELOLA PROMO
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Promo Diskon Toko (${sellerPromos.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = {
                                promoCode = "PROMO-${(100..999).random()}"
                                showCreatePromoDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buat Promo (+)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (sellerPromos.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Belum ada promo aktif untuk toko ini", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(sellerPromos) { promo ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = promo.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Kode: ${promo.promoCode} (Diskon ${promo.discountPercent}%)", color = CyanAccent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text(text = promo.description, color = TextSecondary, fontSize = 11.sp)
                                }
                                IconButton(
                                    onClick = { viewModel.deletePromo(promo.id) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = StatusDanger)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // TAB INFORMASI TOKO & GOOGLE MAPS
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "Detail Informasi Toko", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Text(text = "Nama Toko: ${userProfile?.shopName?.ifBlank { "Toko ZX" }}", color = TextPrimary, fontSize = 13.sp)
                            Text(text = "Handle: @${userProfile?.shopUsername?.ifBlank { "seller" }}", color = CyanPrimary, fontSize = 13.sp)
                            Text(text = "Kode Penjualan: ${userProfile?.salesCode?.ifBlank { "ZXSELL-VERIFIED" }}", color = TextPrimary, fontSize = 13.sp)
                            Text(text = "Kontak WhatsApp: ${myRegistration?.contactPhone ?: "-"}", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "Jam Buka: ${myRegistration?.operatingHours ?: "24 Jam Nonstop"}", color = TextSecondary, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Titik Google Maps Toko:", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = myRegistration?.placeName ?: "Outlet Resmi ZX", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = myRegistration?.formattedAddress ?: "Alamat terverifikasi", color = TextSecondary, fontSize = 11.sp)

                            OutlinedButton(
                                onClick = {
                                    val lat = myRegistration?.latitude ?: -6.2088
                                    val lng = myRegistration?.longitude ?: 106.8456
                                    val mapIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri)
                                    context.startActivity(mapIntent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka Lokasi di Google Maps", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            4 -> {
                // TAB STATISTIK PENJUALAN
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "Statistik Riil Toko Anda", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val paidCount = sellerOrders.count { it.paymentStatus == "paid" }
                            val pendingCount = sellerOrders.count { it.paymentStatus == "pending" }
                            val totalRev = sellerOrders.filter { it.paymentStatus == "paid" }.sumOf { it.total }

                            Text(text = "• Total Pesanan: ${sellerOrders.size}", color = TextPrimary, fontSize = 13.sp)
                            Text(text = "• Pesanan Lunas: $paidCount", color = Color(0xFF00E676), fontSize = 13.sp)
                            Text(text = "• Pesanan Menunggu: $pendingCount", color = StatusWarning, fontSize = 13.sp)
                            Text(text = "• Total Omset Toko: ${formatRupiah(totalRev)}", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Dialog Edit Produk
    if (editingProduct != null) {
        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = { Text("Edit Produk: ${editingProduct!!.name}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it.filter { c -> c.isDigit() } },
                        label = { Text("Harga Produk (Rp)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = editStock,
                        onValueChange = { editStock = it.filter { c -> c.isDigit() } },
                        label = { Text("Jumlah Stok") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = editPrice.toLongOrNull() ?: editingProduct!!.price
                        val s = editStock.toLongOrNull() ?: editingProduct!!.stock
                        viewModel.updateProduct(editingProduct!!.copy(price = p, stock = s))
                        editingProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10))
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Dialog Buat Promo Baru
    if (showCreatePromoDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePromoDialog = false },
            title = { Text("Buat Promo Toko Baru", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = promoTitle,
                        onValueChange = { promoTitle = it },
                        label = { Text("Judul Promo") },
                        placeholder = { Text("Diskon Kilat 15%") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it.uppercase().trim() },
                        label = { Text("Kode Promo") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = promoDiscountPercent,
                        onValueChange = { promoDiscountPercent = it.filter { c -> c.isDigit() } },
                        label = { Text("Diskon (%)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = promoDesc,
                        onValueChange = { promoDesc = it },
                        label = { Text("Deskripsi Promo") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promoTitle.isNotBlank() && promoCode.isNotBlank()) {
                            val newPromo = StorePromo(
                                sellerId = userProfile?.userId ?: "",
                                sellerName = userProfile?.shopName?.ifBlank { "Toko ZX" } ?: "Toko ZX",
                                salesCode = userProfile?.salesCode ?: "",
                                title = promoTitle,
                                description = promoDesc,
                                discountPercent = promoDiscountPercent.toIntOrNull() ?: 10,
                                promoCode = promoCode,
                                isActive = true
                            )
                            viewModel.createPromo(newPromo) {
                                showCreatePromoDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10))
                ) {
                    Text("Aktifkan Promo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePromoDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
