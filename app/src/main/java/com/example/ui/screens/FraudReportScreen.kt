package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentReport
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FraudReportScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var targetTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("FAKE_SELLER") }
    var contactPhone by remember { mutableStateOf("") }
    var reportReason by remember { mutableStateOf("") }
    var evidenceUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val categories = listOf(
        "FAKE_SELLER" to "Penjual Palsu / Bodong",
        "SCAM" to "Penipuan Transaksi",
        "STOLEN_ACCOUNT" to "Akun Hasil Pencurian / Ilegal",
        "QR_ABUSE" to "Penyalahgunaan QRIS Toko",
        "PRICE_MANIPULATION" to "Manipulasi Harga",
        "IMPERSONATION" to "Akun Tiruan Developer / Owner",
        "PROHIBITED_18" to "Produk 18+ / Terlarang",
        "OTHER" to "Penyalahgunaan Nama ZX STORE"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.HELP) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = CyanPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PUSAT PENGADUAN & ANTI PENIPUAN",
                        color = StatusDanger,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Lapor ke Owner & Developer ZX STORE",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Peringatan Keamanan Resmi
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusDanger.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PERINGATAN RESMI ZX SECURITY",
                            color = StatusDanger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hubungi Developer hanya melalui kontak resmi yang ditampilkan di aplikasi (WhatsApp: 087861072798 / 087835461585). Jangan percaya akun atau nomor mana pun yang mengaku sebagai Owner atau Developer tanpa badge resmi '👑 OWNER' atau '⚡ DEVELOPER'.",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Form Laporan
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Pilih Kategori Pelanggaran:",
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { (catKey, catLabel) ->
                            FilterChip(
                                selected = selectedCategory == catKey,
                                onClick = { selectedCategory = catKey },
                                label = { Text(catLabel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusDanger.copy(alpha = 0.25f),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = targetTitle,
                        onValueChange = { targetTitle = it },
                        label = { Text("Nama Toko / Nama Akun / ID Produk yang Dilaporkan *") },
                        placeholder = { Text("Contoh: Toko ABC / Akun Penipu @xxx") },
                        modifier = Modifier.fillMaxWidth().testTag("input_fraud_target"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusDanger,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Nomor WhatsApp Pelapor untuk Konfirmasi *") },
                        placeholder = { Text("Contoh: 081234567890") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = evidenceUrl,
                        onValueChange = { evidenceUrl = it },
                        label = { Text("Link Bukti Screenshot / URL Produk") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Kronologi / Detail Kejadian *") },
                        placeholder = { Text("Jelaskan secara detail bukti penipuan atau pelanggaran yang terjadi...") },
                        modifier = Modifier.fillMaxWidth().testTag("input_fraud_reason"),
                        minLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusDanger,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (targetTitle.isBlank() || reportReason.isBlank()) return@Button
                            isSubmitting = true
                            val report = ContentReport(
                                targetType = "FRAUD",
                                targetId = "target_${System.currentTimeMillis()}",
                                targetTitle = targetTitle,
                                category = selectedCategory,
                                reporterId = userProfile?.userId ?: "",
                                reporterName = userProfile?.displayName ?: "Pelapor",
                                reporterContact = contactPhone,
                                reason = reportReason,
                                evidenceUrl = evidenceUrl,
                                status = "pending"
                            )

                            viewModel.submitFraudReport(report) {
                                isSubmitting = false
                                viewModel.navigateTo(Screen.HELP)
                            }
                        },
                        enabled = !isSubmitting && targetTitle.isNotBlank() && reportReason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusDanger,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_fraud_report_button")
                    ) {
                        Icon(Icons.Default.Report, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSubmitting) "Mengirim Laporan..." else "KIRIM LAPORAN SEKARANG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
