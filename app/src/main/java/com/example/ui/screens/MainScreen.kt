package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ArshadTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel

data class NavItem(
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val badgeCount: Int = 0
)

@Composable
fun MainScreen(
  viewModel: ShopViewModel = viewModel()
) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val wishlistItems by viewModel.wishlistItems.collectAsState()
  val cartItems by viewModel.cartItems.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
  val authSuccessMessage by viewModel.authSuccessMessage.collectAsState()
  val isAiAssistantOpen by viewModel.isAiAssistantOpen.collectAsState()

  val totalCartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }
  val wishlistCount = remember(wishlistItems) { wishlistItems.size }
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(authSuccessMessage) {
    authSuccessMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearAuthMessages()
    }
  }

  // Screen level back handling
  BackHandler(enabled = currentScreen != "main") {
    when (currentScreen) {
      "product_detail", "checkout", "admin_portal", "order_confirmation" -> {
        viewModel.currentScreen.value = "main"
      }
      "order_detail" -> {
        viewModel.currentScreen.value = "main"
        viewModel.selectedTab.value = 4 // Account tab
      }
      "register" -> {
        viewModel.currentScreen.value = "login"
      }
      "login" -> {
        viewModel.currentScreen.value = "main"
      }
      else -> {
        viewModel.currentScreen.value = "main"
      }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    when (currentScreen) {
      "splash" -> {
        SplashScreen(
          onSplashFinished = {
            if (viewModel.isOnboardingCompleted.value) {
              viewModel.currentScreen.value = if (isUserLoggedIn) "main" else "login"
            } else {
              viewModel.currentScreen.value = "onboarding"
            }
          }
        )
      }

      "onboarding" -> {
        OnboardingScreen(
          onFinishOnboarding = {
            viewModel.isOnboardingCompleted.value = true
            viewModel.currentScreen.value = if (isUserLoggedIn) "main" else "login"
          }
        )
      }

    "login" -> {
      LoginScreen(
        viewModel = viewModel,
        onNavigateToRegister = { viewModel.currentScreen.value = "register" },
        onLoginSuccess = {
          viewModel.currentScreen.value = "main"
          viewModel.selectedTab.value = 0
        },
        onContinueAsGuest = {
          viewModel.currentScreen.value = "main"
          viewModel.selectedTab.value = 0
        }
      )
    }

    "register" -> {
      RegisterScreen(
        viewModel = viewModel,
        onNavigateToLogin = { viewModel.currentScreen.value = "login" },
        onRegisterSuccess = {
          viewModel.currentScreen.value = "main"
          viewModel.selectedTab.value = 0
        },
        onContinueAsGuest = {
          viewModel.currentScreen.value = "main"
          viewModel.selectedTab.value = 0
        }
      )
    }

    "product_detail" -> {
      ProductDetailScreen(
        viewModel = viewModel,
        onBackClick = { viewModel.currentScreen.value = "main" },
        onCartClick = {
          viewModel.currentScreen.value = "main"
          viewModel.selectedTab.value = 3 // Cart tab
        },
        onBuyNowClick = {
          viewModel.currentScreen.value = "checkout"
        }
      )
    }

    "checkout" -> {
      CheckoutScreen(
        viewModel = viewModel,
        onBackClick = { viewModel.currentScreen.value = "main"; viewModel.selectedTab.value = 3 }
      )
    }

    "order_confirmation" -> {
      OrderConfirmationScreen(
        viewModel = viewModel,
        onContinueShopping = {
          viewModel.currentScreen.value = "main"
          viewModel.selectedTab.value = 0
        },
        onViewOrders = {
          viewModel.currentScreen.value = "main"
          viewModel.selectedTab.value = 4
        }
      )
    }

    "order_detail" -> {
      OrderDetailScreen(
        viewModel = viewModel,
        onBackClick = {
          viewModel.currentScreen.value = "main"
          viewModel.selectedTab.value = 4
        }
      )
    }

    "admin_portal" -> {
      val isAdmin = isUserLoggedIn && currentUser?.role?.equals("admin", ignoreCase = true) == true
      if (isAdmin) {
        AdminDashboardScreen(
          viewModel = viewModel,
          onExitAdmin = { viewModel.currentScreen.value = "main" }
        )
      } else {
        LaunchedEffect(Unit) {
          viewModel.currentScreen.value = "main"
        }
      }
    }

    "main" -> {
      val navItems = listOf(
        NavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("Categories", Icons.Filled.Category, Icons.Outlined.Category),
        NavItem("Search", Icons.Filled.Search, Icons.Outlined.Search),
        NavItem("Cart", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, badgeCount = totalCartCount),
        NavItem("Account", Icons.Filled.Person, Icons.Outlined.Person)
      )

      Scaffold(
        snackbarHost = {
          SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { data ->
              Snackbar(
                snackbarData = data,
                containerColor = BlackCard,
                contentColor = Color.White,
                actionColor = GoldPrimary,
                shape = RoundedCornerShape(10.dp)
              )
            }
          )
        },
        topBar = {
          ArshadTopBar(
            title = when (selectedTab) {
              0 -> null // Shows luxury Brand Title + Subtitle
              1 -> "CATEGORIES"
              2 -> "SEARCH CATALOG"
              3 -> "MY SHOPPING CART"
              4 -> "MY ACCOUNT & SUPPORT"
              else -> "ARSHAD COLLECTION"
            },
            wishlistCount = wishlistCount,
            cartCount = totalCartCount,
            onSearchClick = { viewModel.selectedTab.value = 2 },
            onWishlistClick = { viewModel.selectedTab.value = 4 },
            onCartClick = { viewModel.selectedTab.value = 3 },
            onNotificationClick = { viewModel.selectedTab.value = 4 }
          )
        },
        bottomBar = {
          NavigationBar(
            containerColor = BlackSurface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            navItems.forEachIndexed { index, item ->
              val isSelected = selectedTab == index

              NavigationBarItem(
                selected = isSelected,
                onClick = { viewModel.selectedTab.value = index },
                icon = {
                  BadgedBox(
                    badge = {
                      if (item.badgeCount > 0) {
                        Badge(
                          containerColor = GoldPrimary,
                          contentColor = BlackMain
                        ) {
                          Text("${item.badgeCount}", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                      }
                    }
                  ) {
                    Icon(
                      imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                      contentDescription = item.title,
                      tint = if (isSelected) GoldPrimary else TextSecondaryDark
                    )
                  }
                },
                label = {
                  Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) GoldPrimary else TextSecondaryDark
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = GoldPrimary.copy(alpha = 0.15f),
                  selectedIconColor = GoldPrimary,
                  unselectedIconColor = TextSecondaryDark,
                  selectedTextColor = GoldPrimary,
                  unselectedTextColor = TextSecondaryDark
                )
              )
            }
          }
        }
      ) { paddingValues ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
          when (selectedTab) {
            0 -> HomeScreen(
              viewModel = viewModel,
              onProductClick = { viewModel.selectProduct(it) },
              onCategoryClick = {
                viewModel.selectedTab.value = 1
              },
              onViewAllClick = {
                viewModel.selectedTab.value = 2
              }
            )

            1 -> CategoriesScreen(
              viewModel = viewModel,
              onProductClick = { viewModel.selectProduct(it) }
            )

            2 -> SearchScreen(
              viewModel = viewModel,
              onProductClick = { viewModel.selectProduct(it) }
            )

            3 -> CartScreen(
              viewModel = viewModel,
              onProceedToCheckout = {
                viewModel.currentScreen.value = "checkout"
              },
              onExploreProducts = {
                viewModel.selectedTab.value = 0
              }
            )

            4 -> AccountScreen(
              viewModel = viewModel,
              onNavigateToOrders = {
                // Open order history or stay in orders section
              },
              onNavigateToWishlist = {
                viewModel.selectedTab.value = 2 // Search/Wishlist explore
              },
              onNavigateToAdmin = {
                viewModel.currentScreen.value = "admin_portal"
              }
            )
          }

          // Floating AI Assistant Button
          FloatingAiAssistantButton(
            onClick = { viewModel.openAiAssistant() },
            modifier = Modifier.align(Alignment.BottomEnd)
          )
        }
      }
    }
  }

  // Interactive AI Assistant Modal Overlay (Root level safe insets viewport)
  AnimatedVisibility(
    visible = isAiAssistantOpen,
    enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
      initialOffsetY = { it / 3 },
      animationSpec = tween(260, easing = FastOutSlowInEasing)
    ),
    exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(
      targetOffsetY = { it / 3 },
      animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
  ) {
    AiAssistantDialog(
      viewModel = viewModel,
      onDismiss = { viewModel.closeAiAssistant() },
      onProductClick = { product ->
        viewModel.selectProduct(product)
      }
    )
  }
}
}
