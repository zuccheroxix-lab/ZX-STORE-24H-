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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OwnerAnnouncement
import com.example.data.model.SellerRegistration
import com.example.data.model.StorePromo
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
fun SellerApprovalCenterTab(
    registrations: List<SellerRegistration>,
    onApprove: (SellerRegistration) -> Unit,
    onReject: (SellerRegistration, String) -> Unit,
    onRevision: (SellerRegistration, String) -> Unit,
    onBlock: (SellerRegistration, String) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("pending_approval") }

    // Dialog action state
    var activeRegForAction by remember { mutableStateOf<SellerRegistration?>(null) }
    var actionType by remember { mutableStateOf("") } // reject, revision, block
    var reasonInput by remember { mutableStateOf("") }

    val filteredList = remember(registrations, selectedFilter) {
        if (selectedFilter == "all") {
            registrations
        } else {
            registrations.filter { it.status == selectedFilter }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HowToReg, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pusat Persetujuan Seller (ACC Owner)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "Verifikasi identitas, lokasi Google Maps, dan terbitkan kode sales resmi ZX-STORE.",
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "pending_approval" to "Menunggu (${registrations.count { it.status == "pending_approval" }})",
                    "approved" to "Disetujui (${registrations.count { it.status == "approved" }})",
                    "revision_requested" to "Revisi (${registrations.count { it.status == "revision_requested" }})",
                    "rejected" to "Ditolak (${registrations.count { it.status == "rejected" }})",
                    "blocked" to "Diblokir (${registrations.count { it.status == "blocked" }})",
                    "all" to "Semua (${registrations.size})"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyanAccent,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada pengajuan seller pada filter ini",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { reg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (reg.status == "pending_approval") CyanAccent.copy(alpha = 0.6f) else SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reg.shopName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "@${reg.shopUsername} • Pemilik: ${reg.ownerFullName}",
                                    color = CyanAccent,
                                    fontSize = 11.sp
                                )
                            }
                            StatusBadge(status = reg.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sales code if assigned
                        if (reg.salesCode.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Kode Sales: ", color = TextMuted, fontSize = 11.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyanPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(reg.salesCode, color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Google Maps Address & Pin
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Titik Google Maps: ${reg.placeName.ifEmpty { "Lokasi Toko" }}",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = reg.address,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                if (reg.latitude != 0.0 || reg.longitude != 0.0) {
                                    Text(
                                        text = "Koordinat: ${reg.latitude}, ${reg.longitude} • Area: ${reg.deliveryCoverage}",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Open in Google Maps
                                    TextButton(
                                        onClick = {
                                            val url = reg.mapsUrl.ifEmpty { "https://maps.google.com/?q=${reg.latitude},${reg.longitude}" }
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Buka di Maps", color = CyanAccent, fontSize = 10.sp)
                                    }

                                    // Direct WA
                                    if (reg.phone.isNotEmpty()) {
                                        TextButton(
                                            onClick = {
                                                val clean = reg.phone.filter { it.isDigit() }
                                                val phoneFormatted = if (clean.startsWith("0")) "62" + clean.substring(1) else clean
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneFormatted"))
                                                context.startActivity(intent)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WA: ${reg.phone}", color = StatusSuccess, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Category & Description
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kategori: ${reg.businessCategory} • Tipe: ${if (reg.isDigitalOnly) "Digital (Voucher/Akun)" else "Fisik & Digital"}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        if (reg.description.isNotEmpty()) {
                            Text(
                                text = "Deskripsi: ${reg.description}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Rejection / Revision reason note if any
                        if (reg.rejectionReason.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Catatan Owner: ${reg.rejectionReason}",
                                color = StatusWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Action Buttons
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (reg.status != "approved") {
                                Button(
                                    onClick = { onApprove(reg) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess, contentColor = Color(0xFF080B10)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ACC SELLER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (reg.status == "pending_approval" || reg.status == "approved") {
                                OutlinedButton(
                                    onClick = {
                                        activeRegForAction = reg
                                        actionType = "revision"
                                        reasonInput = ""
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusWarning),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusWarning),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("REVISI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        activeRegForAction = reg
                                        actionType = "reject"
                                        reasonInput = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("TOLAK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (reg.status != "blocked") {
                                IconButton(
                                    onClick = {
                                        activeRegForAction = reg
                                        actionType = "block"
                                        reasonInput = ""
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceElevated)
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = "Blokir", tint = StatusDanger, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reason Dialog
    if (activeRegForAction != null) {
        val reg = activeRegForAction!!
        val actionLabel = when (actionType) {
            "revision" -> "Minta Revisi Pendaftaran Seller"
            "reject" -> "Tolak Pendaftaran Seller"
            "block" -> "Blokir Akun Seller"
            else -> "Tindakan"
        }

        AlertDialog(
            onDismissRequest = { activeRegForAction = null },
            containerColor = SurfaceDark,
            title = {
                Text(actionLabel, color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Toko: ${reg.shopName} (@${reg.shopUsername})", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("Alasan tindakan (wajib)") },
                        placeholder = { Text("Contoh: Lokasi Maps tidak akurat / nomor WhatsApp tidak aktif...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedLabelColor = CyanPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = reasonInput.ifBlank { "Kebijakan kurasi Owner ZX STORE" }
                        when (actionType) {
                            "revision" -> onRevision(reg, reason)
                            "reject" -> onReject(reg, reason)
                            "block" -> onBlock(reg, reason)
                        }
                        activeRegForAction = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (actionType == "revision") StatusWarning else StatusDanger,
                        contentColor = Color.White
                    )
                ) {
                    Text("Konfirmasi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeRegForAction = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SellerManagementTab(
    registrations: List<SellerRegistration>,
    onBlock: (SellerRegistration, String) -> Unit
) {
    val approvedSellers = remember(registrations) {
        registrations.filter { it.status == "approved" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daftar Penjual Resmi (${approvedSellers.size} Toko)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "Seluruh seller aktif yang telah diverifikasi dan memiliki kode sales resmi ZX STORE.",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        if (approvedSellers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada seller aktif. Silakan setujui pengajuan di tab 'Persetujuan Seller'.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(approvedSellers, key = { it.id }) { seller ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(seller.shopName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "Kode: ${seller.salesCode} • @${seller.shopUsername}",
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Pemilik: ${seller.ownerFullName} • WA: ${seller.phone}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Titik: ${seller.address}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onBlock(seller, "Pelanggaran operasional toko oleh Owner") },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = "Tangguhkan Toko", tint = StatusDanger, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerAnnouncementsTab(
    announcements: List<OwnerAnnouncement>,
    onToggleActive: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onCreate: (title: String, content: String, type: String, actionUrl: String, actionText: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pengumuman Resmi Owner",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Muncul langsung realtime di halaman utama pembeli tanpa perlu restart aplikasi.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (announcements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada pengumuman dibuat. Klik 'Buat Baru' untuk mempublikasikan pengumuman.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(announcements, key = { it.id }) { ann ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (ann.isActive) CyanAccent.copy(alpha = 0.5f) else SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (ann.type == "urgent") Color(0xFFFF5252).copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(ann.type.uppercase(), color = if (ann.type == "urgent") Color(0xFFFF5252) else CyanAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (ann.isActive) "Aktif" else "Nonaktif",
                                    color = if (ann.isActive) StatusSuccess else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = ann.isActive,
                                    onCheckedChange = { onToggleActive(ann.id, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onDelete(ann.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = StatusDanger, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(ann.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(ann.content, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)

                        if (ann.actionUrl.isNotEmpty() && ann.actionText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Aksi: ${ann.actionText} (${ann.actionUrl})", color = CyanAccent, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Author: ${ann.authorName}", color = TextMuted, fontSize = 9.sp)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var titleInput by remember { mutableStateOf("") }
        var contentInput by remember { mutableStateOf("") }
        var typeInput by remember { mutableStateOf("general") }
        var actionUrlInput by remember { mutableStateOf("") }
        var actionTextInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Buat Pengumuman Baru", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Judul Pengumuman") },
                        placeholder = { Text("Contoh: Flash Sale Akhir Pekan 24 Jam") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Isi Pengumuman") },
                        placeholder = { Text("Rincian pengumuman yang ingin disampaikan ke pembeli...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Text("Kategori:", color = TextSecondary, fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val types = listOf("general", "urgent", "promo", "update", "maintenance")
                        items(types) { t ->
                            FilterChip(
                                selected = typeInput == t,
                                onClick = { typeInput = t },
                                label = { Text(t.uppercase(), fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = actionTextInput,
                        onValueChange = { actionTextInput = it },
                        label = { Text("Teks Tombol Aksi (Opsional)") },
                        placeholder = { Text("Contoh: Beli Sekarang") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = actionUrlInput,
                        onValueChange = { actionUrlInput = it },
                        label = { Text("Link URL Aksi (Opsional)") },
                        placeholder = { Text("Contoh: https://wa.me/6287861072798") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                            onCreate(titleInput, contentInput, typeInput, actionUrlInput, actionTextInput)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10))
                ) {
                    Text("Publikasikan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ModerasiPromoTab(
    promos: List<StorePromo>,
    onDeletePromo: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Discount, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Moderasi Kupon & Promo Toko (${promos.size})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "Pantau dan hapus kupon diskon palsu atau tidak wajar yang diterbitkan oleh seller.",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        if (promos.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada kupon promo aktif dari seller.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(promos, key = { it.id }) { promo ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyanPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(promo.code, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (promo.discountType == "percentage") "${promo.discountAmount}% OFF" else "${formatRupiah(promo.discountAmount)} OFF",
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "Toko: ${promo.sellerName} • Min Belanja: ${formatRupiah(promo.minPurchase)}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeletePromo(promo.id) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Promo", tint = StatusDanger, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
