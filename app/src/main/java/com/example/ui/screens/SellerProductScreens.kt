package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Product
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
fun SubmitProductScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("10") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var productType by remember { mutableStateOf("digital") } // "digital" or "physical"
    var deliveryInfo by remember { mutableStateOf("") }
    var productUrl by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var sellerQrUrl by remember { mutableStateOf("") }
    var sellerContact by remember { mutableStateOf("") }

    // Multi-photo URLs
    var newImageUrl by remember { mutableStateOf("") }
    var imageUrls by remember { mutableStateOf(listOf<String>()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AJUKAN PRODUK MARKETPLACE",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Semua pengguna dapat menjual produk di ZX STORE 24H",
                        color = CyanAccent,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { viewModel.navigateTo(Screen.EXPLORE) }) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                }
            }
        }

        // Info Banner Moderation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Produk Anda akan diverifikasi tim ZX STORE 24H untuk menjaga keamanan transaksi bersama.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 1. Basic Info
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Produk") },
                placeholder = { Text("Contoh: Akun MLBB Mythic / Voucher Google Play 100K") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    focusedLabelColor = CyanPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_product_name")
            )
        }

        // Product Type Toggle
        item {
            Text("Jenis Produk", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { productType = "digital" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (productType == "digital") CyanPrimary else SurfaceDark
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Produk Digital (Instan)",
                        color = if (productType == "digital") Color(0xFF080B10) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { productType = "physical" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (productType == "physical") CyanPrimary else SurfaceDark
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Barang Fisik (Kirim)",
                        color = if (productType == "physical") Color(0xFF080B10) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Category selection
        item {
            Text("Pilih Kategori", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    Surface(
                        color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else SurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, if (isSelected) CyanPrimary else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedCategoryId = cat.id }
                    ) {
                        Text(
                            text = cat.name,
                            color = if (isSelected) CyanPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Price & Stock
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { char -> char.isDigit() } },
                    label = { Text("Harga (Rp)") },
                    placeholder = { Text("150000") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_product_price")
                )
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { char -> char.isDigit() } },
                    label = { Text("Stok") },
                    placeholder = { Text("10") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("input_product_stock")
                )
            }
        }

        // Multi-image URLs & preview
        item {
            Text("Foto & Gambar Produk", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newImageUrl,
                    onValueChange = { newImageUrl = it },
                    label = { Text("URL Link Gambar") },
                    placeholder = { Text("https://...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newImageUrl.isNotBlank()) {
                            imageUrls = imageUrls + newImageUrl.trim()
                            newImageUrl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Tambah", color = Color(0xFF080B10), fontWeight = FontWeight.Bold)
                }
            }

            if (imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(imageUrls) { url ->
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Product Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUrls = imageUrls.filterNot { it == url } },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // Description
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi Lengkap") },
                placeholder = { Text("Rincian akun, spesifikasi item, garansi, dsb...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    focusedLabelColor = CyanPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_product_desc")
            )
        }

        // Delivery info & fulfillment
        item {
            OutlinedTextField(
                value = deliveryInfo,
                onValueChange = { deliveryInfo = it },
                label = { Text("Cara Penyerahan / Informasi Pengiriman") },
                placeholder = { Text("Contoh: Dikirim instan via Chat WhatsApp / JNE Reguler") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    focusedLabelColor = CyanPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Contact & Tags
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = sellerContact,
                    onValueChange = { sellerContact = it },
                    label = { Text("No. WhatsApp / Kontak Penjual") },
                    placeholder = { Text("081234567890") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tags (koma)") },
                    placeholder = { Text("MLBB, Murah") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // External Link (optional)
        item {
            OutlinedTextField(
                value = productUrl,
                onValueChange = { productUrl = it },
                label = { Text("Link Eksternal / Demo (Opsional)") },
                placeholder = { Text("https://...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    focusedLabelColor = CyanPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // SELLER PAYMENT QRIS SECTION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "QR Pembayaran / QRIS Penjual",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Masukkan URL foto QRIS Anda agar pembeli dapat mentransfer langsung ke Anda.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sellerQrUrl,
                        onValueChange = { sellerQrUrl = it },
                        label = { Text("URL Gambar QRIS Penjual") },
                        placeholder = { Text("https://... (atau kosongkan untuk menggunakan QRIS Toko)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedLabelColor = CyanPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // QR Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (sellerQrUrl.isNotBlank()) {
                            AsyncImage(
                                model = sellerQrUrl,
                                contentDescription = "Seller QR Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.qris_zucchero_xann),
                                contentDescription = "Official QRIS",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (sellerQrUrl.isNotBlank()) "Pratinjau QRIS Penjual Anda" else "Menggunakan QRIS Resmi ZX STORE / Zucchero Xann",
                        color = CyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Submit Button
        item {
            val selectedCat = categories.find { it.id == selectedCategoryId }
            Button(
                onClick = {
                    val price = priceText.toLongOrNull() ?: 0L
                    val stock = stockText.toLongOrNull() ?: 1L
                    val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val finalImages = if (imageUrls.isNotEmpty()) imageUrls else listOf("https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500")

                    val newProd = Product(
                        name = name,
                        description = description,
                        categoryId = selectedCategoryId,
                        categoryName = selectedCat?.name ?: "Marketplace",
                        price = price,
                        imageUrl = finalImages.firstOrNull() ?: "",
                        images = finalImages,
                        stock = stock,
                        isActive = true,
                        productType = productType,
                        deliveryInfo = deliveryInfo,
                        productUrl = productUrl,
                        tags = tags,
                        sellerId = userProfile?.userId ?: "",
                        sellerName = userProfile?.displayName ?: "Seller",
                        sellerAvatarUrl = userProfile?.avatarUrl ?: "",
                        sellerQrUrl = sellerQrUrl,
                        sellerContact = sellerContact
                    )

                    viewModel.submitProduct(newProd) {
                        viewModel.navigateTo(Screen.PROFILE)
                    }
                },
                enabled = name.isNotBlank() && priceText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_marketplace_product_btn")
            ) {
                Text(
                    text = "Kirim Pengajuan Produk",
                    color = Color(0xFF080B10),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun ModerationDashboardScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProductsForModeration.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val pendingProducts = remember(allProducts) {
        allProducts.filter { it.moderationStatus.equals("pending", ignoreCase = true) }
    }
    val approvedProducts = remember(allProducts) {
        allProducts.filter { it.moderationStatus.equals("approved", ignoreCase = true) }
    }
    val rejectedProducts = remember(allProducts) {
        allProducts.filter { it.moderationStatus.equals("rejected", ignoreCase = true) }
    }
    val revisionProducts = remember(allProducts) {
        allProducts.filter { it.moderationStatus.equals("revision_requested", ignoreCase = true) }
    }

    var inspectingProduct by remember { mutableStateOf<Product?>(null) }
    var actionType by remember { mutableStateOf<String?>(null) } // "reject" or "revision"
    var actionReason by remember { mutableStateOf("") }

    val currentList = when (selectedTab) {
        0 -> pendingProducts
        1 -> approvedProducts
        2 -> rejectedProducts
        3 -> revisionProducts
        else -> pendingProducts
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "DASHBOARD MODERASI",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "Verifikasi pengajuan produk komunitas ZX STORE 24H",
                    color = CyanAccent,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = { viewModel.navigateTo(Screen.PROFILE) }) {
                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
            }
        }

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = StatusWarning.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${pendingProducts.size}", color = StatusWarning, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(text = "Menunggu", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Surface(
                color = StatusSuccess.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${approvedProducts.size}", color = StatusSuccess, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(text = "Disetujui", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Surface(
                color = StatusDanger.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${rejectedProducts.size}", color = StatusDanger, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(text = "Ditolak", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = CyanPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Menunggu (${pendingProducts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Disetujui (${approvedProducts.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Ditolak (${rejectedProducts.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Revisi (${revisionProducts.size})", fontSize = 12.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (currentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada produk dalam status ini",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(currentList) { prod ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BackgroundBlack)
                                    ) {
                                        AsyncImage(
                                            model = prod.imageUrl,
                                            contentDescription = prod.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = prod.name,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatRupiah(prod.price),
                                            color = CyanPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Penjual: ${prod.sellerName}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Stok: ${prod.stock} • Tipe: ${prod.productType}",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                StatusBadge(status = prod.moderationStatus)
                            }

                            if (prod.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = prod.description,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Seller QR inspection trigger
                            if (prod.sellerQrUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceElevated)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "QRIS Penjual Terlampir",
                                        color = CyanAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (prod.rejectionReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = StatusDanger.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Catatan: ${prod.rejectionReason}",
                                        color = StatusDanger,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.moderateProduct(prod.id, "approved") },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Setujui", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        inspectingProduct = prod
                                        actionType = "revision"
                                        actionReason = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusWarning),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Revisi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }

                                Button(
                                    onClick = {
                                        inspectingProduct = prod
                                        actionType = "reject"
                                        actionReason = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tolak", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Rejection / Revision reason note
    if (inspectingProduct != null && actionType != null) {
        val isRejection = actionType == "reject"
        AlertDialog(
            onDismissRequest = {
                inspectingProduct = null
                actionType = null
            },
            title = {
                Text(
                    text = if (isRejection) "Tolak Pengajuan Produk" else "Minta Revisi Produk",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Tuliskan catatan kepada penjual (${inspectingProduct?.sellerName}):",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = actionReason,
                        onValueChange = { actionReason = it },
                        label = { Text("Catatan / Alasan") },
                        placeholder = { Text(if (isRejection) "Produk tidak sesuai kebijakan..." else "Harap perbaiki foto dan harga...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedLabelColor = CyanPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val status = if (isRejection) "rejected" else "revision_requested"
                        inspectingProduct?.let { prod ->
                            viewModel.moderateProduct(prod.id, status, actionReason)
                        }
                        inspectingProduct = null
                        actionType = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRejection) StatusDanger else StatusWarning
                    )
                ) {
                    Text(
                        text = if (isRejection) "Konfirmasi Tolak" else "Kirim Permintaan Revisi",
                        color = if (isRejection) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    inspectingProduct = null
                    actionType = null
                }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun SellerProfileScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val seller = viewModel.selectedSeller.collectAsState().value
    val products = viewModel.selectedSellerProducts.collectAsState().value
    val isFollowing = viewModel.isFollowingSeller.collectAsState().value

    if (seller == null) {
        viewModel.navigateTo(Screen.EXPLORE)
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.EXPLORE) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PROFIL PENJUAL",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }

        // Seller Bio Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(2.dp, CyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (seller.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = seller.avatarUrl,
                                contentDescription = seller.displayName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = seller.displayName.take(1).uppercase(),
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = seller.displayName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "@${seller.username.ifBlank { seller.userId.take(8) }}",
                        color = CyanAccent,
                        fontSize = 12.sp
                    )

                    if (seller.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = seller.bio,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Follow Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${seller.followersCount}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Pengikut", color = TextMuted, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${products.size}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Produk", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.toggleFollowSeller(seller.userId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) SurfaceElevated else CyanPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Mengikuti" else "Ikuti Penjual",
                            color = if (isFollowing) TextPrimary else Color(0xFF080B10),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Seller Products
        item {
            Text(
                text = "PRODUK PENJUAL (${products.size})",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (products.isEmpty()) {
            item {
                Text(
                    text = "Belum ada produk aktif yang disetujui dari penjual ini.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            items(products) { prod ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectProduct(prod) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BackgroundBlack)
                        ) {
                            AsyncImage(
                                model = prod.imageUrl,
                                contentDescription = prod.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prod.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatRupiah(prod.price),
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Stok: ${prod.stock} • ${prod.productType}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
