package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.StoreBottomBar
import com.example.ui.components.TopStoreBar
import com.example.ui.screens.AllProductsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.CreateStoryScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModerationDashboardScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.OrderStatusScreen
import com.example.ui.screens.OwnerDashboardScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QrisPaymentScreen
import com.example.ui.screens.RegisterSellerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.screens.SellerProfileScreen
import com.example.ui.screens.StoryFeedScreen
import com.example.ui.screens.StoryViewerScreen
import com.example.ui.screens.SubmitProductScreen
import com.example.ui.screens.FraudReportScreen
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.ZXStoreTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZXStoreTheme {
                MainAppEntry()
            }
        }
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

@Composable
fun MainAppEntry() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        StoreAppContent(currentUser = currentUser!!)
    }
}

@Composable
fun StoreAppContent(
    currentUser: com.google.firebase.auth.FirebaseUser,
    viewModel: StoreViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser) {
        viewModel.loadUserData(
            uid = currentUser.uid,
            displayName = currentUser.displayName ?: "User",
            email = currentUser.email ?: ""
        )
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Back button handling
    BackHandler(enabled = currentScreen != Screen.HOME) {
        when (currentScreen) {
            Screen.PRODUCT_DETAIL -> viewModel.navigateTo(Screen.EXPLORE)
            Screen.EXPLORE -> viewModel.navigateTo(Screen.HOME)
            Screen.SUBMIT_PRODUCT -> viewModel.navigateTo(Screen.HOME)
            Screen.STORY -> viewModel.navigateTo(Screen.HOME)
            Screen.CREATE_STORY -> viewModel.navigateTo(Screen.STORY)
            Screen.STORY_VIEWER -> viewModel.navigateTo(Screen.STORY)
            Screen.SELLER_PROFILE -> viewModel.navigateTo(Screen.EXPLORE)
            Screen.MODERATION_DASHBOARD -> viewModel.navigateTo(Screen.PROFILE)
            Screen.CHECKOUT -> viewModel.navigateTo(Screen.CART)
            Screen.QRIS_PAYMENT -> viewModel.navigateTo(Screen.ORDER_HISTORY)
            Screen.ORDER_STATUS -> viewModel.navigateTo(Screen.ORDER_HISTORY)
            Screen.SEARCH -> viewModel.navigateTo(Screen.HOME)
            Screen.CATEGORIES -> viewModel.navigateTo(Screen.HOME)
            Screen.PROFILE -> viewModel.navigateTo(Screen.HOME)
            Screen.HELP -> viewModel.navigateTo(Screen.HOME)
            Screen.REGISTER_SELLER -> viewModel.navigateTo(Screen.PROFILE)
            Screen.SELLER_DASHBOARD -> viewModel.navigateTo(Screen.PROFILE)
            Screen.FRAUD_REPORT -> viewModel.navigateTo(Screen.HELP)
            Screen.ADMIN_PRODUCTS, Screen.ADMIN_ORDERS, Screen.ADMIN_CATEGORIES, Screen.ADMIN_SETTINGS -> {
                viewModel.navigateTo(Screen.OWNER_DASHBOARD)
            }
            Screen.OWNER_DASHBOARD -> viewModel.navigateTo(Screen.HOME)
            else -> viewModel.navigateTo(Screen.HOME)
        }
    }

    val isOwner = viewModel.isOwner || viewModel.isDeveloper

    Scaffold(
        topBar = {
            TopStoreBar(
                storeName = storeSettings.storeName,
                cartItemCount = cart.items.sumOf { it.quantity.toInt() },
                isOwner = isOwner,
                onCartClick = { viewModel.navigateTo(Screen.CART) },
                onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                onAdminClick = { viewModel.navigateTo(Screen.OWNER_DASHBOARD) }
            )
        },
        bottomBar = {
            StoreBottomBar(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundBlack,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundBlack)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(viewModel = viewModel)
                Screen.EXPLORE, Screen.ALL_PRODUCTS -> ExploreScreen(viewModel = viewModel)
                Screen.SUBMIT_PRODUCT -> SubmitProductScreen(viewModel = viewModel)
                Screen.STORY -> StoryFeedScreen(viewModel = viewModel)
                Screen.CREATE_STORY -> CreateStoryScreen(viewModel = viewModel)
                Screen.STORY_VIEWER -> StoryViewerScreen(viewModel = viewModel)
                Screen.SELLER_PROFILE -> SellerProfileScreen(viewModel = viewModel)
                Screen.MODERATION_DASHBOARD -> ModerationDashboardScreen(viewModel = viewModel)
                Screen.CATEGORIES -> CategoriesScreen(viewModel = viewModel)
                Screen.PRODUCT_DETAIL -> ProductDetailScreen(viewModel = viewModel)
                Screen.SEARCH -> SearchScreen(viewModel = viewModel)
                Screen.CART -> CartScreen(viewModel = viewModel)
                Screen.CHECKOUT -> CheckoutScreen(viewModel = viewModel)
                Screen.QRIS_PAYMENT -> QrisPaymentScreen(viewModel = viewModel)
                Screen.ORDER_STATUS -> OrderStatusScreen(viewModel = viewModel)
                Screen.ORDER_HISTORY -> OrderHistoryScreen(viewModel = viewModel)
                Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
                Screen.HELP -> HelpScreen(viewModel = viewModel)
                Screen.REGISTER_SELLER -> RegisterSellerScreen(viewModel = viewModel)
                Screen.SELLER_DASHBOARD -> SellerDashboardScreen(viewModel = viewModel)
                Screen.FRAUD_REPORT -> FraudReportScreen(viewModel = viewModel)
                Screen.OWNER_DASHBOARD,
                Screen.ADMIN_PRODUCTS,
                Screen.ADMIN_CATEGORIES,
                Screen.ADMIN_ORDERS,
                Screen.ADMIN_VERIFY_PAYMENT,
                Screen.ADMIN_SETTINGS -> OwnerDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
