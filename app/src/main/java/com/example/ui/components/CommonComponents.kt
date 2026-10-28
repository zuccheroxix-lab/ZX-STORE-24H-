package com.example.ui.components

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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ZX",
                        color = Color(0xFF080B10),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = storeName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "24H DIGITAL STORE",
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
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
                        contentDescription = "Cari Produk",
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
    isOwner: Boolean,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = SurfaceDark,
        contentColor = TextSecondary,
        tonalElevation = 8.dp,
        modifier = modifier.border(width = 1.dp, color = SurfaceBorder)
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF080B10),
                selectedTextColor = CyanPrimary,
                indicatorColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.ALL_PRODUCTS || currentScreen == Screen.CATEGORIES,
            onClick = { onNavigate(Screen.ALL_PRODUCTS) },
            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Produk") },
            label = { Text("Produk", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF080B10),
                selectedTextColor = CyanPrimary,
                indicatorColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_products")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.ORDER_HISTORY,
            onClick = { onNavigate(Screen.ORDER_HISTORY) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Pesanan") },
            label = { Text("Pesanan", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF080B10),
                selectedTextColor = CyanPrimary,
                indicatorColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_orders")
        )

        if (isOwner) {
            NavigationBarItem(
                selected = currentScreen == Screen.OWNER_DASHBOARD ||
                        currentScreen == Screen.ADMIN_PRODUCTS ||
                        currentScreen == Screen.ADMIN_ORDERS,
                onClick = { onNavigate(Screen.OWNER_DASHBOARD) },
                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Owner") },
                label = { Text("Owner", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF080B10),
                    selectedTextColor = CyanAccent,
                    indicatorColor = CyanAccent,
                    unselectedIconColor = CyanAccent.copy(alpha = 0.7f),
                    unselectedTextColor = CyanAccent.copy(alpha = 0.7f)
                ),
                modifier = Modifier.testTag("nav_owner")
            )
        }

        NavigationBarItem(
            selected = currentScreen == Screen.PROFILE,
            onClick = { onNavigate(Screen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil", fontSize = 11.sp) },
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
        "pending", "awaiting_payment" -> Triple(StatusWarning.copy(alpha = 0.15f), StatusWarning, "Menunggu Pembayaran")
        "proof_submitted", "awaiting_verification" -> Triple(CyanPrimary.copy(alpha = 0.15f), CyanPrimary, "Verifikasi Owner")
        "paid", "completed" -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, "Lunas / Selesai")
        "processing" -> Triple(StatusWarning.copy(alpha = 0.2f), StatusWarning, "Sedang Diproses")
        "rejected", "cancelled" -> Triple(StatusDanger.copy(alpha = 0.15f), StatusDanger, "Dibatalkan / Ditolak")
        "expired" -> Triple(Color.Gray.copy(alpha = 0.2f), Color.LightGray, "Kadaluarsa")
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
