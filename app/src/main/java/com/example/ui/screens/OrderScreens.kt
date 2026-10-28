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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.model.StoreOrder
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OrderStatusScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val order = viewModel.selectedOrder.collectAsState().value
    val context = LocalContext.current
    val storeSettings by viewModel.storeSettings.collectAsState()

    if (order == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundBlack),
            contentAlignment = Alignment.Center
        ) {
            Text("Pesanan tidak ditemukan", color = TextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.ORDER_HISTORY) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Status Pesanan",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.orderCode,
                        color = CyanAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Live Tracker Card
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
                        Text(
                            text = "Status Saat Ini",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        StatusBadge(status = order.paymentStatus)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 1: Menunggu Pembayaran
                    TrackerStep(
                        title = "1. Pesanan Dibuat",
                        desc = "Total: ${formatRupiah(order.total)} via QRIS",
                        isCompleted = true,
                        isCurrent = order.paymentStatus == "pending"
                    )

                    TrackerDivider()

                    // Step 2: Menunggu Verifikasi
                    TrackerStep(
                        title = "2. Bukti Pembayaran",
                        desc = if (order.paymentStatus == "pending") "Belum dikirim oleh pelanggan" else "Bukti terkirim, menunggu verifikasi owner",
                        isCompleted = order.paymentStatus != "pending",
                        isCurrent = order.paymentStatus == "proof_submitted"
                    )

                    TrackerDivider()

                    // Step 3: Verifikasi Lunas
                    TrackerStep(
                        title = "3. Pembayaran Diverifikasi",
                        desc = if (order.paymentStatus == "paid") "Pembayaran lunas terkonfirmasi oleh owner" else "Menunggu konfirmasi owner",
                        isCompleted = order.paymentStatus == "paid",
                        isCurrent = order.orderStatus == "processing"
                    )

                    TrackerDivider()

                    // Step 4: Selesai / Pengiriman
                    TrackerStep(
                        title = "4. Pesanan Selesai",
                        desc = if (order.orderStatus == "completed") "Produk / voucher telah dikirim" else "Dalam antrian pemrosesan",
                        isCompleted = order.orderStatus == "completed",
                        isCurrent = order.orderStatus == "completed"
                    )
                }
            }
        }

        // Digital Delivery Notes (If Fulfilled)
        if (order.digitalFulfillmentNotes.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pengiriman / Kode Voucher",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = order.digitalFulfillmentNotes,
                            color = CyanAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Items Purchased
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Item Pesanan",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    order.items.forEach { rawItem ->
                        val name = rawItem["name"] as? String ?: ""
                        val qty = (rawItem["quantity"] as? Number)?.toLong() ?: 1L
                        val price = (rawItem["price"] as? Number)?.toLong() ?: 0L
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${qty}x $name",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatRupiah(price * qty),
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceBorder))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(formatRupiah(order.total), color = CyanPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (order.paymentStatus == "pending") {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.QRIS_PAYMENT) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = Color(0xFF080B10)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Pembayaran QRIS", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        val contact = storeSettings.supportContact.ifBlank { "628123456789" }
                        val cleanPhone = if (contact.startsWith("0")) "62" + contact.drop(1) else contact
                        val text = "Halo Owner ZX STORE 24H, saya ingin konfirmasi pesanan saya dengan kode ${order.orderCode} (Total: ${formatRupiah(order.total)})."
                        val url = "https://wa.me/$cleanPhone?text=${Uri.encode(text)}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = StatusSuccess
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat Owner via WhatsApp", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.userOrders.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Riwayat Pesanan Saya",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Daftar pesanan aktif dan selesai di ZX STORE 24H.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Belum ada riwayat pesanan", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.ALL_PRODUCTS) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = Color(0xFF080B10)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mulai Belanja", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(orders) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectOrder(order) }
                    .testTag("order_item_${order.orderCode}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.orderCode,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        StatusBadge(status = order.paymentStatus)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${order.items.size} item produk • ${formatRupiah(order.total)}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kontak: ${order.customerContact}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Detail", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val orders by viewModel.userOrders.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = androidx.credentials.CredentialManager.create(context)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Profil Pengguna",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // User Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile?.displayName?.take(1)?.uppercase() ?: "U",
                            color = Color(0xFF080B10),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile?.displayName?.ifBlank { "Customer" } ?: "Customer",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = userProfile?.email ?: "",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (userProfile?.role == "owner") CyanAccent.copy(alpha = 0.2f)
                                    else SurfaceElevated
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (userProfile?.role == "owner") "👑 OWNER / DEVELOPER" else "CUSTOMER",
                                color = if (userProfile?.role == "owner") CyanAccent else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Total Pesanan", color = TextSecondary, fontSize = 11.sp)
                        Text(orders.size.toString(), color = CyanPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pesanan Selesai", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            orders.count { it.orderStatus == "completed" }.toString(),
                            color = StatusSuccess,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Menu Links
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (userProfile?.role == "owner") {
                        ProfileMenuItem(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Dashboard Owner & Verifikasi",
                            subtitle = "Kelola pesanan, katalog, verifikasi QRIS",
                            onClick = { viewModel.navigateTo(Screen.OWNER_DASHBOARD) }
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceBorder))
                    }

                    ProfileMenuItem(
                        icon = Icons.Default.ReceiptLong,
                        title = "Riwayat Pesanan",
                        subtitle = "Lihat status transaksi dan QRIS",
                        onClick = { viewModel.navigateTo(Screen.ORDER_HISTORY) }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceBorder))

                    ProfileMenuItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Pusat Bantuan & Layanan 24H",
                        subtitle = "FAQ, tata cara bayar QRIS & kontak CS",
                        onClick = { viewModel.navigateTo(Screen.HELP) }
                    )
                }
            }
        }

        // Sign Out Button
        item {
            Button(
                onClick = {
                    signOut(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = {
                            viewModel.navigateTo(Screen.HOME)
                        },
                        scope = coroutineScope
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceElevated,
                    contentColor = com.example.ui.theme.StatusDanger
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.StatusDanger.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_btn")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari Akun (Logout)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HelpScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val storeSettings by viewModel.storeSettings.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text(
                    text = "Bantuan & Layanan 24H",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // WhatsApp Direct Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kontak Owner / Customer Support",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Nomor WhatsApp: ${storeSettings.supportContact.ifBlank { "081234567890" }}",
                        color = StatusSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Layanan aktif 24 jam nonstop untuk verifikasi pembayaran, kendala pesanan, atau pertanyaan produk.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val contact = storeSettings.supportContact.ifBlank { "628123456789" }
                            val cleanPhone = if (contact.startsWith("0")) "62" + contact.drop(1) else contact
                            val text = "Halo ZX STORE 24H, saya membutuhkan bantuan terkait transaksi saya."
                            val url = "https://wa.me/$cleanPhone?text=${Uri.encode(text)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusSuccess,
                            contentColor = Color(0xFF080B10)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hubungi via WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // FAQ Section
        item {
            Text(
                text = "Pertanyaan Umum (FAQ)",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FaqItem(
                question = "Apakah toko benar-benar aktif 24 jam?",
                answer = "Ya, ZX STORE 24H beroperasi 24 jam nonstop via cloud backend Firebase. Anda dapat melakukan pemesanan dan pembayaran kapan pun."
            )
        }

        item {
            FaqItem(
                question = "Bagaimana cara membayar dengan QRIS?",
                answer = "Pilih produk, lakukan checkout, lalu scan gambar QRIS resmi ZUCCHERO XANN (NMID: ID1026503273710) dari aplikasi M-Banking atau E-Wallet mana pun. Masukkan nominal tagihan secara tepat."
            )
        }

        item {
            FaqItem(
                question = "Berapa lama verifikasi pembayaran?",
                answer = "Setelah Anda mengunggah bukti pembayaran atau nomor referensi transfer di halaman QRIS, owner akan memverifikasi dalam waktu 1-5 menit."
            )
        }

        item {
            FaqItem(
                question = "Bagaimana pengiriman voucher / produk digital?",
                answer = "Setelah status pembayaran dinyatakan lunas oleh owner, kode voucher atau diamond akan langsung dikirimkan ke nomor kontak WhatsApp yang Anda daftarkan."
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(question, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(answer, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun TrackerStep(
    title: String,
    desc: String,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) StatusSuccess
                    else if (isCurrent) CyanPrimary
                    else SurfaceElevated
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF080B10), modifier = Modifier.size(14.dp))
            } else {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF080B10)))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = if (isCompleted || isCurrent) TextPrimary else TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = desc,
                color = if (isCurrent) CyanAccent else TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TrackerDivider() {
    Box(
        modifier = Modifier
            .padding(start = 10.dp, top = 2.dp, bottom = 2.dp)
            .width(2.dp)
            .height(20.dp)
            .background(SurfaceBorder)
    )
}
