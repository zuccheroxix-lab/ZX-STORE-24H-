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
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SellerRegistration
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

data class PlacePreset(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val placeId: String
)

@Composable
fun RegisterSellerScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val myRegistration by viewModel.mySellerRegistration.collectAsState()

    // Form fields
    var applicantName by remember { mutableStateOf(userProfile?.displayName ?: "") }
    var shopName by remember { mutableStateOf(myRegistration?.shopName ?: "") }
    var shopUsername by remember { mutableStateOf(myRegistration?.shopUsername ?: "") }
    var shopLogoUrl by remember { mutableStateOf(myRegistration?.shopLogoUrl ?: "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80") }
    var shopDescription by remember { mutableStateOf(myRegistration?.shopDescription ?: "") }
    var contactPhone by remember { mutableStateOf(myRegistration?.contactPhone ?: "") }
    var category by remember { mutableStateOf(myRegistration?.category ?: "JB ALL GAME") }
    var operatingHours by remember { mutableStateOf(myRegistration?.operatingHours ?: "24 Jam Nonstop") }
    var sellerQrUrl by remember { mutableStateOf(myRegistration?.sellerQrUrl ?: userProfile?.sellerQrUrl ?: "") }
    var sampleProductUrl by remember { mutableStateOf(myRegistration?.sampleProductImageUrl ?: "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&auto=format&fit=crop&q=80") }
    var serviceTerms by remember { mutableStateOf(myRegistration?.serviceTerms ?: "Akun / produk 100% legal, garansi anti hack back, transaksi aman terverifikasi ZX STORE.") }

    // Google Maps Location
    var locationType by remember { mutableStateOf(myRegistration?.locationType ?: "STORE_FRONT") } // STORE_FRONT or DELIVERY_AREA
    var placeSearchQuery by remember { mutableStateOf("") }
    var placeName by remember { mutableStateOf(myRegistration?.placeName ?: "ZX Gaming Hub Jakarta") }
    var formattedAddress by remember { mutableStateOf(myRegistration?.formattedAddress ?: "Jl. Sudirman No. 24, Jakarta Pusat, DKI Jakarta 10220") }
    var latitude by remember { mutableDoubleStateOf(if (myRegistration != null) myRegistration!!.latitude else -6.2088) }
    var longitude by remember { mutableDoubleStateOf(if (myRegistration != null) myRegistration!!.longitude else 106.8456) }
    var placeId by remember { mutableStateOf(myRegistration?.placeId ?: "ChIJ8T1_place_zx") }

    // Initial Promo fields
    var promoName by remember { mutableStateOf(myRegistration?.promoName ?: "Diskon Buka Toko ZX") }
    var promoDescription by remember { mutableStateOf(myRegistration?.promoDescription ?: "Promo spesial pelanggan baru diskon s/d 20%") }
    var promoOriginalPrice by remember { mutableStateOf(if (myRegistration != null && myRegistration!!.promoOriginalPrice > 0) myRegistration!!.promoOriginalPrice.toString() else "100000") }
    var promoDiscountedPrice by remember { mutableStateOf(if (myRegistration != null && myRegistration!!.promoDiscountedPrice > 0) myRegistration!!.promoDiscountedPrice.toString() else "80000") }
    var promoPeriod by remember { mutableStateOf(myRegistration?.promoPeriod ?: "30 Hari Pertama") }
    var promoBannerUrl by remember { mutableStateOf(myRegistration?.promoBannerUrl ?: "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80") }

    var isSubmitting by remember { mutableStateOf(false) }

    val presetLocations = remember {
        listOf(
            PlacePreset("ZX Gaming Center Jakarta", "Jl. Jend. Sudirman Kav. 24, Jakarta Pusat", -6.2088, 106.8456, "place_jkt_01"),
            PlacePreset("ZX Store Malioboro Hub", "Jl. Malioboro No. 88, Yogyakarta", -7.7928, 110.3658, "place_jog_02"),
            PlacePreset("ZX Outlet Tunjungan", "Jl. Tunjungan No. 12, Surabaya, Jawa Timur", -7.2625, 112.7388, "place_sby_03"),
            PlacePreset("ZX Delivery Area Bandung", "Area Pelayanan Kota Bandung & Sekitarnya", -6.9175, 107.6191, "place_bdg_04"),
            PlacePreset("ZX Digital Delivery Medan", "Area Pelayanan Kota Medan, Sumatera Utara", 3.5952, 98.6722, "place_mdn_05")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header Bar
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
                Column {
                    Text(
                        text = "PENDAFTARAN SELLER",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "ZX STORE 24 JAM MARKETPLACE",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Status Banner jika sudah pernah mendaftar
        if (myRegistration != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (myRegistration!!.status) {
                            "approved" -> StatusSuccess
                            "rejected", "blocked" -> StatusDanger
                            else -> StatusWarning
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status Pendaftaran Anda",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            StatusBadge(status = myRegistration!!.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (myRegistration!!.status) {
                            "pending_approval" -> {
                                Text(
                                    text = "Pendaftaran toko Anda sedang dalam antrean peninjauan oleh Owner. Anda akan menerima notifikasi segera setelah di-ACC.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kode Toko Sementara: ${myRegistration!!.salesCode}",
                                        color = CyanAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            "approved" -> {
                                Text(
                                    text = "Selamat! Toko Anda telah resmi DISETUJUI oleh Owner. Anda sekarang dapat mengunggah dan mengelola produk di Dashboard Seller.",
                                    color = StatusSuccess,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.navigateTo(Screen.SELLER_DASHBOARD) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF080B10)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Buka Dashboard Seller", fontWeight = FontWeight.Bold)
                                }
                            }
                            "revision_requested" -> {
                                Text(
                                    text = "Owner meminta perbaikan data: \"${myRegistration!!.rejectionReason}\". Silakan perbaiki form di bawah dan kirim ulang.",
                                    color = StatusWarning,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            "rejected" -> {
                                Text(
                                    text = "Pendaftaran ditolak: \"${myRegistration!!.rejectionReason}\". Anda dapat memperbarui informasi dan mengajukan ulang pendaftaran.",
                                    color = StatusDanger,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            "blocked" -> {
                                Text(
                                    text = "Akun seller ini telah ditangguhkan/diblokir oleh sistem moderasi karena pelanggaran ketentuan marketplace.",
                                    color = StatusDanger,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Ketentuan & Larangan Resmi (Anti Penipuan & 18+)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ketentuan & Kebijakan Penjual ZX",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Dilarang keras konten 18+, pornografi, penipuan/scam, dan akun curian.\n• Khusus JB ALL GAME: Wajib kepemilikan sah, anti-hack back, dan garansi pembeli.\n• Seller wajib di-ACC resmi oleh Owner sebelum toko dan produk aktif publik.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 4. BAGIAN A: Identitas Toko
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "A. Identitas Toko",
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Nama Toko *") },
                        placeholder = { Text("Contoh: Zuxann Gaming Store") },
                        modifier = Modifier.fillMaxWidth().testTag("input_shop_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = shopUsername,
                        onValueChange = { shopUsername = it.lowercase().trim().replace(" ", "_") },
                        label = { Text("Username Toko (Unik) *") },
                        prefix = { Text("@", color = CyanAccent) },
                        placeholder = { Text("zuxanngaming") },
                        modifier = Modifier.fillMaxWidth().testTag("input_shop_username"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = { applicantName = it },
                        label = { Text("Nama Pemilik Toko *") },
                        placeholder = { Text("Nama lengkap sesuai identitas") },
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
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Nomor Kontak Toko (WhatsApp) *") },
                        placeholder = { Text("Contoh: 081234567890") },
                        modifier = Modifier.fillMaxWidth().testTag("input_contact_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = shopLogoUrl,
                        onValueChange = { shopLogoUrl = it },
                        label = { Text("URL Foto / Logo Toko") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = shopDescription,
                        onValueChange = { shopDescription = it },
                        label = { Text("Deskripsi Toko *") },
                        placeholder = { Text("Jelaskan produk yang Anda jual dan keunggulan toko Anda...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // 5. BAGIAN B: Lokasi Google Maps
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "B. Lokasi Google Maps",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = CyanAccent)
                    }

                    Text(
                        text = "Tentukan titik outlet atau area jangkauan pelayanan toko Anda di Google Maps. Anda dapat memilih area pelayanan tanpa harus mempublikasikan alamat rumah pribadi.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    // Pilihan Tipe Lokasi
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = locationType == "STORE_FRONT",
                            onClick = { locationType = "STORE_FRONT" },
                            label = { Text("Toko Fisik / Outlet") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Color(0xFF080B10)
                            )
                        )
                        FilterChip(
                            selected = locationType == "DELIVERY_AREA",
                            onClick = { locationType = "DELIVERY_AREA" },
                            label = { Text("Area Pelayanan / Online") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Color(0xFF080B10)
                            )
                        )
                    }

                    // Pencarian Lokasi Google Maps
                    OutlinedTextField(
                        value = placeSearchQuery,
                        onValueChange = { placeSearchQuery = it },
                        label = { Text("Cari Lokasi / Daerah Toko") },
                        placeholder = { Text("Ketik nama mall, jalan, atau kota...") },
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    // Pilihan Preset Cepat Lokasi
                    Text(text = "Rekomendasi / Pilihan Titik:", color = TextSecondary, fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetLocations) { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (placeName == preset.name) CyanPrimary.copy(alpha = 0.2f) else SurfaceElevated)
                                    .border(1.dp, if (placeName == preset.name) CyanPrimary else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        placeName = preset.name
                                        formattedAddress = preset.address
                                        latitude = preset.lat
                                        longitude = preset.lng
                                        placeId = preset.placeId
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = preset.name,
                                    color = if (placeName == preset.name) CyanPrimary else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Card Preview Google Maps
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = placeName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = formattedAddress, color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Koordinat: $latitude, $longitude (Place ID: $placeId)",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    val mapIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri)
                                    context.startActivity(mapIntent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka di Google Maps", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 6. BAGIAN C: Informasi Penjualan & QRIS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "C. Informasi Penjualan & Pembayaran",
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori Utama *") },
                        placeholder = { Text("JB ALL GAME / Top Up / Voucher / Jasa") },
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
                        value = operatingHours,
                        onValueChange = { operatingHours = it },
                        label = { Text("Jam Operasional *") },
                        placeholder = { Text("24 Jam Nonstop / 09:00 - 23:00 WIB") },
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
                        value = sellerQrUrl,
                        onValueChange = { sellerQrUrl = it },
                        label = { Text("URL / Gambar QRIS Toko Penjual") },
                        placeholder = { Text("Masukkan URL gambar QRIS toko Anda") },
                        leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = CyanPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = sampleProductUrl,
                        onValueChange = { sampleProductUrl = it },
                        label = { Text("Foto Contoh Produk Utama") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = serviceTerms,
                        onValueChange = { serviceTerms = it },
                        label = { Text("Ketentuan Garansi & Layanan *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // 7. BAGIAN D: Informasi Promosi Toko
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "D. Promo Pembukaan Toko (Opsional)",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = CyanAccent)
                    }

                    OutlinedTextField(
                        value = promoName,
                        onValueChange = { promoName = it },
                        label = { Text("Nama Promo") },
                        placeholder = { Text("Diskon Opening Sale 20%") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = promoOriginalPrice,
                            onValueChange = { promoOriginalPrice = it.filter { char -> char.isDigit() } },
                            label = { Text("Harga Asli (Rp)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = promoDiscountedPrice,
                            onValueChange = { promoDiscountedPrice = it.filter { char -> char.isDigit() } },
                            label = { Text("Harga Promo (Rp)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = promoPeriod,
                        onValueChange = { promoPeriod = it },
                        label = { Text("Masa Berlaku Promo") },
                        placeholder = { Text("Contoh: 14 Hari / Sampai Akhir Bulan") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // 8. Tombol Kirim Pendaftaran
        item {
            Button(
                onClick = {
                    if (shopName.isBlank() || shopUsername.isBlank() || contactPhone.isBlank()) {
                        return@Button
                    }
                    isSubmitting = true
                    val registration = SellerRegistration(
                        id = myRegistration?.id ?: "",
                        userId = userProfile?.userId ?: "",
                        userEmail = userProfile?.email ?: "",
                        applicantName = applicantName.ifBlank { "Seller ZX" },
                        shopName = shopName,
                        shopUsername = shopUsername,
                        shopLogoUrl = shopLogoUrl,
                        shopDescription = shopDescription,
                        contactPhone = contactPhone,
                        category = category,
                        productTypes = listOf("digital", "physical", "jasa"),
                        locationType = locationType,
                        placeName = placeName,
                        formattedAddress = formattedAddress,
                        latitude = latitude,
                        longitude = longitude,
                        placeId = placeId,
                        mapsUrl = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude",
                        operatingHours = operatingHours,
                        paymentMethods = listOf("QRIS", "Transfer Bank", "E-Wallet"),
                        sellerQrUrl = sellerQrUrl,
                        sampleProductImageUrl = sampleProductUrl,
                        serviceTerms = serviceTerms,
                        promoName = promoName,
                        promoDescription = promoDescription,
                        promoOriginalPrice = promoOriginalPrice.toLongOrNull() ?: 0L,
                        promoDiscountedPrice = promoDiscountedPrice.toLongOrNull() ?: 0L,
                        promoPeriod = promoPeriod,
                        promoBannerUrl = promoBannerUrl,
                        status = "pending_approval"
                    )

                    viewModel.submitSellerRegistration(registration) {
                        isSubmitting = false
                        viewModel.navigateTo(Screen.PROFILE)
                    }
                },
                enabled = !isSubmitting && shopName.isNotBlank() && shopUsername.isNotBlank() && contactPhone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = Color(0xFF080B10)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_seller_registration_button")
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSubmitting) "Mengirim Data Pendaftaran..." else "KIRIM PENDAFTARAN SELLER KE OWNER",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}
