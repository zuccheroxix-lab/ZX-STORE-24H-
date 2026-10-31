package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Story
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.formatRupiah
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StoryCarousel(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val stories by viewModel.stories.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .border(width = 0.5.dp, color = SurfaceBorder)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STORY 24 JAM",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "${stories.size} Aktif",
                color = CyanAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Add Story" Button for logged-in user
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.CREATE_STORY) }
                        .testTag("add_story_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.5.dp, CyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = userProfile?.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Story",
                                tint = Color(0xFF080B10),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cerita Anda",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Active Stories from users
            items(stories) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.selectStory(story) }
                        .testTag("story_item_${story.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        CyanPrimary,
                                        Color(0xFF00E5FF),
                                        Color(0xFF2979FF),
                                        CyanPrimary
                                    )
                                )
                            )
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(BackgroundBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            if (story.authorAvatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = story.authorAvatarUrl,
                                    contentDescription = story.authorName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = story.authorName.take(1).uppercase(),
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.authorName.take(8),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StoryFeedScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val stories by viewModel.stories.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ZX STORY 24 JAM",
                            color = CyanPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bagikan testimoni, promo produk toko, atau update langsung kepada pembeli & komunitas.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo(Screen.CREATE_STORY) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.testTag("create_story_main_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF080B10),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Posting", color = Color(0xFF080B10), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (stories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Story Aktif",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Jadilah yang pertama memposting story 24 jam untuk toko Anda!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(stories) { story ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectStory(story) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, CyanPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (story.authorAvatarUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = story.authorAvatarUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(story.authorName.take(1), color = CyanPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = story.authorName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val privacyIcon = when (story.privacyMode.uppercase()) {
                                            "PUBLIC" -> Icons.Default.Public
                                            "FOLLOWERS" -> Icons.Default.People
                                            else -> Icons.Default.Lock
                                        }
                                        Icon(
                                            imageVector = privacyIcon,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = story.privacyMode.lowercase(),
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${story.viewersCount}",
                                    color = CyanAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (story.mediaType == "PHOTO" && story.mediaUrl.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BackgroundBlack)
                            ) {
                                AsyncImage(
                                    model = story.mediaUrl,
                                    contentDescription = "Story Media",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            val bgColor = try {
                                Color(android.graphics.Color.parseColor(story.backgroundColorHex))
                            } catch (_: Exception) {
                                Color(0xFF0A192F)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = story.textContent,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        if (story.caption.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = story.caption,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (story.linkedProductId.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = SurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Produk Terkait: ${story.linkedProductName}",
                                        color = CyanPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryViewerScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val story = viewModel.selectedStory.collectAsState().value
    val currentUser = viewModel.userProfile.collectAsState().value
    val products by viewModel.products.collectAsState()
    val isDeveloper = viewModel.isDeveloper

    if (story == null) {
        viewModel.navigateTo(Screen.STORY)
        return
    }

    val isAuthor = story.authorId == currentUser?.userId
    val canDelete = isAuthor || isDeveloper

    var showReportDialog by remember { mutableStateOf(false) }
    var showViewersDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    var isPaused by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }

    val reactionEmojis = listOf("🔥", "❤️", "⚡", "👏", "💎")

    // Hold-to-pause progress timer
    LaunchedEffect(story.id, isPaused) {
        val durationMs = (story.durationSeconds.coerceAtLeast(4)) * 1000L
        val stepMs = 50L
        while (currentProgress < 1f) {
            delay(stepMs)
            if (!isPaused) {
                currentProgress += (stepMs.toFloat() / durationMs.toFloat())
            }
        }
        viewModel.navigateTo(Screen.STORY)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        if (offset.x < size.width * 0.35f) {
                            // Tap left - previous/exit
                            viewModel.navigateTo(Screen.STORY)
                        } else {
                            // Tap right - next/exit
                            viewModel.navigateTo(Screen.STORY)
                        }
                    }
                )
            }
    ) {
        // --- 1. Story Media Rendering by Type ---
        when (story.mediaType.uppercase()) {
            "PHOTO" -> {
                if (story.mediaUrl.isNotBlank()) {
                    AsyncImage(
                        model = story.mediaUrl,
                        contentDescription = "Story Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            "VIDEO" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070B12)),
                    contentAlignment = Alignment.Center
                ) {
                    if (story.mediaUrl.isNotBlank()) {
                        AsyncImage(
                            model = story.mediaUrl,
                            contentDescription = "Video Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(2.dp, CyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = CyanPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }
            "TESTIMONIAL" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF041C2C), Color(0xFF080B10), Color(0xFF0A2540))
                            )
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.9f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = story.textContent.ifBlank { "Layanan ZX STORE 24 Jam sangat cepat dan terpercaya!" },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "— Verified Customer ZX STORE",
                                color = CyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            else -> {
                // TEXT or fallback
                val bgColor = try {
                    Color(android.graphics.Color.parseColor(story.backgroundColorHex))
                } catch (_: Exception) {
                    Color(0xFF051622)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = story.textContent,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        lineHeight = 30.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Top Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
        )

        // Bottom Gradient overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                    )
                )
        )

        // --- 2. Top Bar & Progress ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CyanPrimary,
                trackColor = Color.White.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, CyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (story.authorAvatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = story.authorAvatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(story.authorName.take(1), color = CyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = story.authorName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (story.authorUsername.isNotBlank()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "@${story.authorUsername}",
                                    color = CyanAccent,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "24 Jam • ${story.privacyMode}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            if (isPaused) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Dijeda",
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canDelete) {
                        IconButton(onClick = { viewModel.deleteStory(story.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Story", tint = StatusDanger)
                        }
                    }
                    if (!isAuthor) {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(Icons.Default.Flag, contentDescription = "Laporkan", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.STORY) }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }
            }
        }

        // --- 3. Bottom Actions: Viewers, Reactions, Product & Caption ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Viewers info button (Tapping shows real viewers dialog)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceBorder),
                    modifier = Modifier.clickable {
                        if (canDelete || story.viewersProfiles.isNotEmpty()) {
                            showViewersDialog = true
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${story.viewersCount} Dilihat ${if (canDelete) "• Lihat Akun" else ""}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Caption
            if (story.caption.isNotBlank()) {
                Text(
                    text = story.caption,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Linked Product Card
            if (story.linkedProductId.isNotBlank()) {
                val linkedProd = products.find { it.id == story.linkedProductId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.95f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            if (linkedProd != null) viewModel.selectProduct(linkedProd)
                            else viewModel.navigateTo(Screen.EXPLORE)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = story.linkedProductName.ifBlank { "Lihat Produk Terkait" },
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (story.linkedProductPrice > 0L) {
                                    Text(
                                        text = formatRupiah(story.linkedProductPrice),
                                        color = CyanAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (linkedProd != null) viewModel.selectProduct(linkedProd)
                                else viewModel.navigateTo(Screen.EXPLORE)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Beli", color = Color(0xFF080B10), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Realtime Emoji Reaction Tray
            val myReaction = currentUser?.userId?.let { story.userReactions[it] }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(0.5.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                reactionEmojis.forEach { emoji ->
                    val count = story.reactions[emoji] ?: 0L
                    val isSelected = myReaction == emoji
                    Surface(
                        color = if (isSelected) CyanPrimary.copy(alpha = 0.25f) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary) else null,
                        modifier = Modifier
                            .clickable { viewModel.reactToStory(story.id, emoji) }
                            .padding(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                            if (count > 0L) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$count",
                                    color = if (isSelected) CyanPrimary else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Viewers List Dialog (Real users only)
    if (showViewersDialog) {
        AlertDialog(
            onDismissRequest = { showViewersDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pemirsa Story (${story.viewersCount})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                if (story.viewersProfiles.isEmpty()) {
                    Text(
                        text = "Belum ada rekaman profil pemirsa atau story baru dibuka.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(story.viewersProfiles) { viewer ->
                            val name = viewer["displayName"] ?: "Member ZX"
                            val uname = viewer["username"] ?: "user"
                            val avatar = viewer["avatarUrl"] ?: ""
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceElevated)
                                        .border(1.dp, CyanPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatar.isNotBlank()) {
                                        AsyncImage(
                                            model = avatar,
                                            contentDescription = name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(name.take(1).uppercase(), color = CyanPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "@$uname", color = CyanAccent, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showViewersDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Tutup", color = Color(0xFF080B10))
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Report Story Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Laporkan Story", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Apakah story ini melanggar pedoman komunitas ZX STORE?",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Alasan Pelaporan") },
                        placeholder = { Text("Spam, konten ilegal, penipuan...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedLabelColor = CyanPrimary,
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
                        if (reportReason.isNotBlank()) {
                            viewModel.reportContent("STORY", story.id, story.authorName, reportReason)
                            showReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Kirim Laporan", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReportDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun CreateStoryScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val products by viewModel.products.collectAsState()
    val userSearchResults by viewModel.userSearchResults.collectAsState()

    var storyType by remember { mutableStateOf("TEXT") } // "TEXT", "PHOTO", "VIDEO", "TESTIMONIAL", "PRODUCT_LINKED"
    var textContent by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#051622") }
    var selectedProductId by remember { mutableStateOf("") }
    var privacyMode by remember { mutableStateOf("PUBLIC") } // "PUBLIC", "FOLLOWERS", "PRIVATE", "SELECTED_ACCOUNTS", "HIDE_FROM"

    var selectedUsers by remember { mutableStateOf<List<com.example.data.model.UserProfile>>(emptyList()) }
    var showUserSearchDialog by remember { mutableStateOf(false) }
    var userSearchQuery by remember { mutableStateOf("") }

    val bgColors = listOf("#051622", "#1B2A4A", "#0D324D", "#3B185F", "#1A1A24", "#002B36")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BUAT STORY 24 JAM",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                IconButton(onClick = { viewModel.navigateTo(Screen.STORY) }) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                }
            }
        }

        // Story Type Selector (Scrollable Row)
        item {
            Text("Tipe Story:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            val types = listOf(
                "TEXT" to "Teks",
                "PHOTO" to "Foto",
                "VIDEO" to "Video",
                "TESTIMONIAL" to "Testimoni",
                "PRODUCT_LINKED" to "Produk"
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(types) { (type, label) ->
                    val isSelected = storyType == type
                    Button(
                        onClick = { storyType = type },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) CyanPrimary else SurfaceDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF080B10) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Live Preview Box
        item {
            Text("Pratinjau Story", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            val previewBg = try {
                Color(android.graphics.Color.parseColor(selectedColorHex))
            } catch (_: Exception) {
                Color(0xFF051622)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (storyType == "TEXT") previewBg else SurfaceDark)
                    .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (storyType) {
                    "TEXT" -> {
                        Text(
                            text = textContent.ifBlank { "Tulis cerita atau promo Anda..." },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    "PHOTO" -> {
                        if (mediaUrl.isNotBlank()) {
                            AsyncImage(
                                model = mediaUrl,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("Masukkan URL foto di bawah", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                    "VIDEO" -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Pratinjau Media Video 24 Jam", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(if (mediaUrl.isNotBlank()) mediaUrl else "Masukkan URL stream/video", color = CyanAccent, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                    "TESTIMONIAL" -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${textContent.ifBlank { "Layanan cepat dan terpercaya!" }}\"",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("— Pembeli Terverifikasi ZX", color = CyanAccent, fontSize = 11.sp)
                        }
                    }
                    "PRODUCT_LINKED" -> {
                        val prod = products.find { it.id == selectedProductId }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = prod?.name ?: "Pilih produk toko di bawah",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (prod != null) {
                                Text(formatRupiah(prod.price), color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Color selector if TEXT
        if (storyType == "TEXT") {
            item {
                Text("Warna Latar Belakang", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    bgColors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColorHex == hex) 2.dp else 0.dp,
                                    color = if (selectedColorHex == hex) CyanPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }
            }
        }

        // Content inputs depending on type
        if (storyType in listOf("TEXT", "TESTIMONIAL")) {
            item {
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    label = { Text(if (storyType == "TESTIMONIAL") "Isi Testimoni" else "Teks Story") },
                    placeholder = { Text("Contoh: Promo Top Up kilat hanya hari ini di ZX STORE!") },
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
        }

        if (storyType in listOf("PHOTO", "VIDEO")) {
            item {
                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text(if (storyType == "PHOTO") "URL Foto / Gambar" else "URL Video / Stream") },
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
        }

        // Caption
        item {
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Keterangan / Caption (Opsional)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    focusedLabelColor = CyanPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Link to a Product
        item {
            Text("Hubungkan Produk Toko (Opsional)", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val isNone = selectedProductId.isBlank()
                    Surface(
                        color = if (isNone) CyanPrimary.copy(alpha = 0.2f) else SurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, if (isNone) CyanPrimary else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedProductId = "" }
                    ) {
                        Text(
                            text = "Tanpa Produk",
                            color = if (isNone) CyanPrimary else TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                items(products) { prod ->
                    val isSelected = selectedProductId == prod.id
                    Surface(
                        color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else SurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, if (isSelected) CyanPrimary else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedProductId = prod.id }
                    ) {
                        Text(
                            text = prod.name.take(18),
                            color = if (isSelected) CyanPrimary else TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // PER-ACCOUNT STORY PRIVACY WITH REAL REGISTERED USER SEARCH
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Privasi & Target Pemirsa Story",
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val privacyOptions = listOf(
                        "PUBLIC" to "Publik (Semua Pengguna Marketplace)",
                        "FOLLOWERS" to "Hanya Pengikut (Followers)",
                        "PRIVATE" to "Pribadi (Hanya Saya)",
                        "SELECTED_ACCOUNTS" to "Pilih Akun Tertentu (Target Pengguna Terdaftar)",
                        "HIDE_FROM" to "Sembunyikan dari Akun Tertentu"
                    )

                    privacyOptions.forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { privacyMode = mode }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = privacyMode == mode,
                                onClick = { privacyMode = mode },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = label, color = TextPrimary, fontSize = 12.sp)
                        }
                    }

                    // User selection chips & search launcher
                    if (privacyMode in listOf("SELECTED_ACCOUNTS", "HIDE_FROM")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (privacyMode == "SELECTED_ACCOUNTS") "Akun yang dipilih (${selectedUsers.size})" else "Akun yang disembunyikan (${selectedUsers.size})",
                                color = CyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = {
                                    userSearchQuery = ""
                                    viewModel.clearUserSearchResults()
                                    showUserSearchDialog = true
                                },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cari @Username", color = CyanPrimary, fontSize = 11.sp)
                            }
                        }

                        if (selectedUsers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(selectedUsers) { u ->
                                    Surface(
                                        color = SurfaceElevated,
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("@${u.username}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Hapus",
                                                tint = StatusDanger,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        selectedUsers = selectedUsers.filterNot { it.userId == u.userId }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Submit Button
        item {
            val linkedProduct = products.find { it.id == selectedProductId }
            Button(
                onClick = {
                    val allowedUids = if (privacyMode == "SELECTED_ACCOUNTS") selectedUsers.map { it.userId } else emptyList()
                    val hiddenUids = if (privacyMode == "HIDE_FROM") selectedUsers.map { it.userId } else emptyList()

                    val newStory = Story(
                        authorId = userProfile?.userId ?: "",
                        authorName = userProfile?.displayName ?: "ZX User",
                        authorUsername = userProfile?.username ?: "",
                        authorAvatarUrl = userProfile?.avatarUrl ?: "",
                        mediaType = storyType,
                        mediaUrl = mediaUrl,
                        textContent = textContent,
                        backgroundColorHex = selectedColorHex,
                        caption = caption,
                        linkedProductId = linkedProduct?.id ?: "",
                        linkedProductName = linkedProduct?.name ?: "",
                        linkedProductPrice = linkedProduct?.price ?: 0L,
                        privacyMode = privacyMode,
                        allowedUserIds = allowedUids,
                        hiddenUserIds = hiddenUids
                    )

                    viewModel.createStory(newStory) {
                        viewModel.navigateTo(Screen.STORY)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("publish_story_btn")
            ) {
                Text(
                    text = "Unggah Story (Tayang 24 Jam)",
                    color = Color(0xFF080B10),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }

    // Registered Username Search Dialog
    if (showUserSearchDialog) {
        AlertDialog(
            onDismissRequest = { showUserSearchDialog = false },
            title = {
                Text(
                    text = "Cari Pengguna Terdaftar ZX",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = {
                            userSearchQuery = it
                            viewModel.searchUsers(it)
                        },
                        label = { Text("Ketik username (cth: candra, admin)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            focusedLabelColor = CyanPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (userSearchResults.isEmpty()) {
                        Text(
                            text = if (userSearchQuery.isBlank()) "Ketik nama untuk mencari akun di database ZX" else "Pengguna tidak ditemukan",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(userSearchResults) { user ->
                                val alreadyAdded = selectedUsers.any { it.userId == user.userId }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!alreadyAdded) {
                                                selectedUsers = selectedUsers + user
                                            } else {
                                                selectedUsers = selectedUsers.filterNot { it.userId == user.userId }
                                            }
                                        }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Column {
                                        Text(user.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("@${user.username}", color = CyanAccent, fontSize = 11.sp)
                                    }
                                    if (alreadyAdded) {
                                        Text("✓ Terpilih", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("+ Tambah", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showUserSearchDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Selesai", color = Color(0xFF080B10))
                }
            },
            containerColor = SurfaceDark
        )
    }
}
