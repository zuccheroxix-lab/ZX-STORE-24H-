package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.Screen
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
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace("Rp", "Rp ").replace(",00", "")
}

@Composable
fun TopStoreBar(
    storeName: String,
    cartItemCount: Int,
    isOwner: Boolean,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Official ZX Emblem
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, CyanPrimary, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zx_official_emblem),
                        contentDescription = "ZX Emblem",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = storeName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "V2",
                                color = CyanPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "COMMUNITY MARKETPLACE • 24H",
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("top_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Marketplace",
                        tint = TextPrimary
                    )
                }

                if (isOwner) {
                    IconButton(
                        onClick = onAdminClick,
                        modifier = Modifier.testTag("top_admin_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Dashboard Owner",
                            tint = CyanAccent
                        )
                    }
                }

                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.testTag("top_cart_btn")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(
                                    containerColor = CyanPrimary,
                                    contentColor = Color(0xFF080B10)
                                ) {
                                    Text(
                                        text = cartItemCount.toString(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Keranjang Belanja",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = SurfaceDark,
        contentColor = TextSecondary,
        tonalElevation = 8.dp,
        modifier = modifier.border(width = 1.dp, color = SurfaceBorder)
    ) {
        // 1. Home
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF080B10),
                selectedTextColor = CyanPrimary,
                indicatorColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_home")
        )

        // 2. Jelajah / Explore
        NavigationBarItem(
            selected = currentScreen == Screen.EXPLORE || currentScreen == Screen.ALL_PRODUCTS || currentScreen == Screen.CATEGORIES,
            onClick = { onNavigate(Screen.EXPLORE) },
            icon = { Icon(Icons.Default.Explore, contentDescription = "Jelajah") },
            label = { Text("Jelajah", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF080B10),
                selectedTextColor = CyanPrimary,
                indicatorColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_explore")
        )

        // 3. Tambah (+) / Jual
        NavigationBarItem(
            selected = currentScreen == Screen.SUBMIT_PRODUCT,
            onClick = { onNavigate(Screen.SUBMIT_PRODUCT) },
            icon = {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = "Jual Produk",
                    tint = if (currentScreen == Screen.SUBMIT_PRODUCT) Color(0xFF080B10) else CyanPrimary,
                    modifier = Modifier.size(26.dp)
                )
            },
            label = { Text("Jual (+)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF080B10),
                selectedTextColor = CyanPrimary,
                indicatorColor = CyanPrimary,
                unselectedIconColor = CyanPrimary,
                unselectedTextColor = CyanPrimary
            ),
            modifier = Modifier.testTag("nav_sell")
        )

        // 4. Story 24H
        NavigationBarItem(
            selected = currentScreen == Screen.STORY || currentScreen == Screen.STORY_VIEWER,
            onClick = { onNavigate(Screen.STORY) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Story") },
            label = { Text("Story", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF080B10),
                selectedTextColor = CyanPrimary,
                indicatorColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_story")
        )

        // 5. Profil
        NavigationBarItem(
            selected = currentScreen == Screen.PROFILE || currentScreen == Screen.ORDER_HISTORY,
            onClick = { onNavigate(Screen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF080B10),
                selectedTextColor = CyanPrimary,
                indicatorColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "pending", "awaiting_payment", "pending_approval" -> Triple(StatusWarning.copy(alpha = 0.15f), StatusWarning, "Menunggu Persetujuan")
        "proof_submitted", "awaiting_verification" -> Triple(CyanPrimary.copy(alpha = 0.15f), CyanPrimary, "Verifikasi Owner")
        "paid", "completed" -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, "Lunas / Selesai")
        "processing" -> Triple(StatusWarning.copy(alpha = 0.2f), StatusWarning, "Sedang Diproses")
        "rejected", "cancelled" -> Triple(StatusDanger.copy(alpha = 0.15f), StatusDanger, "Ditolak / Batal")
        "blocked" -> Triple(StatusDanger.copy(alpha = 0.25f), StatusDanger, "Akun Diblokir")
        "expired" -> Triple(Color.Gray.copy(alpha = 0.2f), Color.LightGray, "Kadaluarsa")
        "approved" -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, "Disetujui")
        "revision_requested" -> Triple(StatusWarning.copy(alpha = 0.2f), StatusWarning, "Minta Revisi")
        else -> Triple(Color.DarkGray, Color.White, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.5.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RoleBadge(
    role: String,
    isDeveloper: Boolean = false,
    isOwner: Boolean = false,
    sellerStatus: String = "none",
    modifier: Modifier = Modifier
) {
    val (badgeLabel, borderCol, textCol) = when {
        isOwner || role == "owner" -> Triple("👑 OWNER", Color(0xFFFFD54F), Color(0xFFFFE082))
        isDeveloper || role == "developer" -> Triple("⚡ DEVELOPER", CyanAccent, CyanAccent)
        role == "seller" || sellerStatus == "approved" -> Triple("🏪 SELLER", Color(0xFF69F0AE), Color(0xFFB9F6CA))
        else -> Triple("👤 USER", SurfaceBorder, TextSecondary)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                when {
                    isOwner || role == "owner" -> Color(0xFFFF8F00).copy(alpha = 0.2f)
                    isDeveloper || role == "developer" -> CyanAccent.copy(alpha = 0.2f)
                    role == "seller" || sellerStatus == "approved" -> Color(0xFF00C853).copy(alpha = 0.2f)
                    else -> SurfaceElevated
                }
            )
            .border(1.dp, borderCol.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeLabel,
            color = textCol,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
