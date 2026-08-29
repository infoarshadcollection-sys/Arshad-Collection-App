package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ArshadDatabase
import com.example.data.model.*
import com.example.data.repository.ArshadRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
  RELEVANCE("Relevance"),
  NEWEST("Newest Arrivals"),
  PRICE_LOW_HIGH("Price: Low to High"),
  PRICE_HIGH_LOW("Price: High to Low"),
  BEST_RATED("Best Rated"),
  MOST_POPULAR("Most Popular")
}

data class FilterState(
  val selectedCategoryId: String? = null,
  val minPrice: Double = 0.0,
  val maxPrice: Double = 25000.0,
  val inStockOnly: Boolean = false,
  val selectedSize: String? = null,
  val selectedColor: String? = null,
  val minRating: Float = 0.0f,
  val discountOnly: Boolean = false
)

data class CartSummary(
  val subtotal: Double = 0.0,
  val discount: Double = 0.0,
  val deliveryFee: Double = 200.0,
  val grandTotal: Double = 0.0,
  val freeDeliveryThreshold: Double = 3000.0,
  val amountNeededForFreeDelivery: Double = 0.0,
  val freeDeliveryProgress: Float = 0f
)

data class CheckoutForm(
  val fullName: String = "",
  val phone: String = "",
  val email: String = "",
  val address: String = "",
  val city: String = "Karachi",
  val area: String = "",
  val postalCode: String = "",
  val deliveryInstructions: String = "",
  val deliveryMethod: String = "Standard Courier (2-4 Days)",
  val paymentMethod: String = "Cash on Delivery"
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {

  private val database = ArshadDatabase.getDatabase(application)
  val repository = ArshadRepository(database)

  // Navigation / UI State
  val currentScreen = MutableStateFlow("splash") // splash, onboarding, login, register, forgot_password, main, product_detail, checkout, order_confirmation, order_detail, admin_portal
  val selectedTab = MutableStateFlow(0) // 0: Home, 1: Categories, 2: Search, 3: Cart, 4: Account
  val isOnboardingCompleted = MutableStateFlow(false)

  // Auth & User State
  private val sharedPrefs = application.getSharedPreferences("arshad_auth_prefs", android.content.Context.MODE_PRIVATE)
  val currentUser = MutableStateFlow<User?>(null)
  val isUserLoggedIn = MutableStateFlow(false)
  val authLoading = MutableStateFlow(false)
  val authError = MutableStateFlow<String?>(null)
  val authSuccessMessage = MutableStateFlow<String?>(null)

  init {
    // Ensure admin account is seeded and restore persistent session if available
    viewModelScope.launch {
      repository.ensureAdminAccountSeeded()
      val savedUid = sharedPrefs.getString("active_user_uid", null)
      if (!savedUid.isNullOrBlank()) {
        val user = repository.getUserByUid(savedUid)
        if (user != null) {
          currentUser.value = user
          isUserLoggedIn.value = true
        }
      }
    }
  }

  // Catalog State
  val allProducts = repository.allActiveProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allProductsAdmin = repository.allProductsAdmin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val categoriesAdmin = repository.allCategoriesAdmin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val banners = repository.activeBanners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val featuredProducts = repository.featuredProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val bestSellers = repository.bestSellers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val newArrivals = repository.newArrivals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val flashSaleProducts = repository.flashSaleProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val lowStockProducts = repository.lowStockProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val appSettings = repository.appSettings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

  // Search & Filter State
  val searchQuery = MutableStateFlow("")
  val searchFilter = MutableStateFlow(FilterState())
  val sortOption = MutableStateFlow(SortOption.RELEVANCE)
  val recentSearches = MutableStateFlow(listOf("Shalwar Kameez", "Bedsheets", "Abaya", "Multani Pottery", "Smart Watch"))
  val popularSearches = listOf("Men's Kurta", "Embroidered Suit", "King Bed Sheet", "Curtains", "Crockery Set", "Gadgets")

  val filteredProducts: StateFlow<List<Product>> = combine(
    allProducts,
    searchQuery,
    searchFilter,
    sortOption
  ) { products, query, filter, sort ->
    var result = products.filter { product ->
      val matchesQuery = query.isBlank() ||
          product.name.contains(query, ignoreCase = true) ||
          product.description.contains(query, ignoreCase = true) ||
          product.brand.contains(query, ignoreCase = true) ||
          product.sku.contains(query, ignoreCase = true) ||
          product.tags.any { it.contains(query, ignoreCase = true) }

      val matchesCategory = filter.selectedCategoryId == null || product.categoryId == filter.selectedCategoryId
      val matchesPrice = product.currentPrice in filter.minPrice..filter.maxPrice
      val matchesStock = !filter.inStockOnly || product.isInStock
      val matchesRating = product.rating >= filter.minRating
      val matchesDiscount = !filter.discountOnly || product.discountPercent > 0
      val matchesSize = filter.selectedSize == null || product.sizes.contains(filter.selectedSize)
      val matchesColor = filter.selectedColor == null || product.colors.contains(filter.selectedColor)

      matchesQuery && matchesCategory && matchesPrice && matchesStock && matchesRating && matchesDiscount && matchesSize && matchesColor
    }

    result = when (sort) {
      SortOption.RELEVANCE -> result
      SortOption.NEWEST -> result.sortedByDescending { it.createdAt }
      SortOption.PRICE_LOW_HIGH -> result.sortedBy { it.currentPrice }
      SortOption.PRICE_HIGH_LOW -> result.sortedByDescending { it.currentPrice }
      SortOption.BEST_RATED -> result.sortedByDescending { it.rating }
      SortOption.MOST_POPULAR -> result.sortedByDescending { it.reviewCount }
    }
    result
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Wishlist
  val wishlistItems = repository.wishlistItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Cart & Pricing
  val cartItems = repository.cartItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val appliedCouponCode = MutableStateFlow("")
  val couponDiscount = MutableStateFlow(0.0)
  val couponErrorMessage = MutableStateFlow<String?>(null)

  val cartSummary: StateFlow<CartSummary> = combine(
    cartItems,
    couponDiscount,
    appSettings
  ) { items, discount, settings ->
    val subtotal = items.sumOf { it.itemTotal }
    val freeThreshold = settings.freeDeliveryThreshold
    val fee = if (subtotal >= freeThreshold || subtotal == 0.0) 0.0 else settings.defaultDeliveryFee
    val grandTotal = maxOf(0.0, subtotal - discount + fee)
    val needed = maxOf(0.0, freeThreshold - subtotal)
    val progress = (subtotal / freeThreshold).toFloat().coerceIn(0f, 1f)

    CartSummary(
      subtotal = subtotal,
      discount = discount,
      deliveryFee = fee,
      grandTotal = grandTotal,
      freeDeliveryThreshold = freeThreshold,
      amountNeededForFreeDelivery = needed,
      freeDeliveryProgress = progress
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartSummary())

  // Selected Product Detail State
  val selectedProduct = MutableStateFlow<Product?>(null)
  val selectedSize = MutableStateFlow("")
  val selectedColor = MutableStateFlow("")
  val selectedQuantity = MutableStateFlow(1)
  val productReviews = MutableStateFlow<List<Review>>(emptyList())

  // Checkout State
  val checkoutForm = MutableStateFlow(CheckoutForm())
  val isPlacingOrder = MutableStateFlow(false)
  val orderPlacementError = MutableStateFlow<String?>(null)
  val lastPlacedOrder = MutableStateFlow<CustomerOrder?>(null)

  // Orders State
  val customerOrders = repository.allOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val selectedOrder = MutableStateFlow<CustomerOrder?>(null)

  // Admin State (Derived securely from the authenticated user's role)
  val isAdminLoggedIn: StateFlow<Boolean> = currentUser.map { user ->
    user?.role?.equals("admin", ignoreCase = true) == true
  }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
  val adminSelectedTab = MutableStateFlow(0) // 0: Dashboard, 1: Products, 2: Categories, 3: Orders, 4: Inventory, 5: Coupons, 6: Settings

  // Notifications
  val notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Flash Sale Timer
  val flashSaleSecondsLeft = MutableStateFlow(14 * 3600 + 45 * 60 + 22) // 14h 45m 22s

  init {
    // Flash sale countdown loop
    viewModelScope.launch {
      while (true) {
        delay(1000)
        flashSaleSecondsLeft.value = maxOf(0, flashSaleSecondsLeft.value - 1)
      }
    }
  }

  fun selectProduct(product: Product) {
    selectedProduct.value = product
    selectedSize.value = product.sizes.firstOrNull() ?: ""
    selectedColor.value = product.colors.firstOrNull() ?: ""
    selectedQuantity.value = 1

    viewModelScope.launch {
      repository.getReviewsForProduct(product.id).collect { reviews ->
        productReviews.value = reviews
      }
    }
    currentScreen.value = "product_detail"
  }

  fun toggleWishlist(productId: Long) {
    viewModelScope.launch {
      repository.toggleWishlist(productId)
    }
  }

  fun addToCart(product: Product, size: String = "", color: String = "", quantity: Int = 1) {
    viewModelScope.launch {
      val finalSize = if (size.isNotBlank()) size else product.sizes.firstOrNull() ?: ""
      val finalColor = if (color.isNotBlank()) color else product.colors.firstOrNull() ?: ""
      repository.addToCart(product.id, finalSize, finalColor, quantity)
    }
  }

  fun updateCartItemQuantity(cartItemId: Long, newQty: Int) {
    viewModelScope.launch {
      repository.updateCartQuantity(cartItemId, newQty)
    }
  }

  fun removeCartItem(cartItemId: Long) {
    viewModelScope.launch {
      repository.removeCartItem(cartItemId)
    }
  }

  fun clearCart() {
    viewModelScope.launch {
      repository.clearCart()
      couponDiscount.value = 0.0
      appliedCouponCode.value = ""
    }
  }

  fun applyCoupon(code: String) {
    viewModelScope.launch {
      couponErrorMessage.value = null
      val subtotal = cartSummary.value.subtotal
      val (valid, discount) = repository.validateCoupon(code, subtotal)
      if (valid) {
        appliedCouponCode.value = code.uppercase().trim()
        couponDiscount.value = discount
      } else {
        couponErrorMessage.value = "Invalid or expired coupon code, or minimum amount not met."
      }
    }
  }

  fun removeCoupon() {
    appliedCouponCode.value = ""
    couponDiscount.value = 0.0
    couponErrorMessage.value = null
  }

  fun submitReview(productId: Long, customerName: String, rating: Float, reviewText: String) {
    viewModelScope.launch {
      repository.submitReview(productId, customerName, rating, reviewText)
    }
  }

  fun placeOrder() {
    viewModelScope.launch {
      val form = checkoutForm.value
      if (form.fullName.isBlank() || form.phone.isBlank() || form.address.isBlank() || form.city.isBlank()) {
        orderPlacementError.value = "Please complete all mandatory contact & delivery fields."
        return@launch
      }

      val items = cartItems.value
      if (items.isEmpty()) {
        orderPlacementError.value = "Your cart is empty."
        return@launch
      }

      isPlacingOrder.value = true
      orderPlacementError.value = null

      val result = repository.placeOrder(
        customerName = form.fullName.ifBlank { currentUser.value?.username ?: "" },
        phone = form.phone,
        email = form.email.ifBlank { currentUser.value?.email ?: "" },
        address = form.address,
        city = form.city,
        area = form.area,
        postalCode = form.postalCode,
        deliveryInstructions = form.deliveryInstructions,
        itemsToPurchase = items,
        couponCode = appliedCouponCode.value,
        paymentMethod = form.paymentMethod,
        deliveryFeeConfig = appSettings.value.defaultDeliveryFee,
        freeDeliveryThreshold = appSettings.value.freeDeliveryThreshold,
        userId = currentUser.value?.uid ?: "guest_user"
      )

      isPlacingOrder.value = false

      result.onSuccess { order ->
        lastPlacedOrder.value = order
        currentScreen.value = "order_confirmation"
        appliedCouponCode.value = ""
        couponDiscount.value = 0.0
      }.onFailure { error ->
        orderPlacementError.value = error.message ?: "Failed to place order. Please try again."
      }
    }
  }

  fun selectOrder(order: CustomerOrder) {
    selectedOrder.value = order
    currentScreen.value = "order_detail"
  }

  fun performSearch(query: String) {
    searchQuery.value = query
    if (query.isNotBlank() && !recentSearches.value.contains(query)) {
      recentSearches.value = (listOf(query) + recentSearches.value).take(8)
    }
  }

  fun clearSearch() {
    searchQuery.value = ""
  }

  fun updateFilter(filter: FilterState) {
    searchFilter.value = filter
  }

  fun resetFilter() {
    searchFilter.value = FilterState()
  }

  // Admin Actions (Authorized against currentUser)
  fun adminUpdateOrderStatus(orderId: Long, newStatus: OrderStatus) {
    viewModelScope.launch {
      repository.updateOrderStatus(orderId, newStatus, currentUser.value)
    }
  }

  fun adminSaveProduct(product: Product) {
    viewModelScope.launch {
      if (product.id == 0L) {
        repository.saveProduct(product, currentUser.value)
      } else {
        repository.updateProduct(product, currentUser.value)
      }
    }
  }

  fun adminDeleteProduct(id: Long) {
    viewModelScope.launch {
      repository.deleteProduct(id, currentUser.value)
    }
  }

  fun adminUpdateStock(id: Long, newStock: Int) {
    viewModelScope.launch {
      repository.updateStock(id, newStock, currentUser.value)
    }
  }

  fun adminSaveCategory(category: Category) {
    viewModelScope.launch {
      repository.saveCategory(category, currentUser.value)
    }
  }

  fun adminDeleteCategory(id: String) {
    viewModelScope.launch {
      repository.deleteCategory(id, currentUser.value)
    }
  }

  fun adminSaveCoupon(coupon: Coupon) {
    viewModelScope.launch {
      repository.saveCoupon(coupon, currentUser.value)
    }
  }

  fun adminDeleteCoupon(code: String) {
    viewModelScope.launch {
      repository.deleteCoupon(code, currentUser.value)
    }
  }

  fun adminUpdateSettings(settings: AppSettings) {
    viewModelScope.launch {
      repository.updateSettings(settings, currentUser.value)
    }
  }

  // --- Customer Authentication Actions ---

  fun register(
    email: String,
    username: String,
    password: String,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      authLoading.value = true
      authError.value = null
      authSuccessMessage.value = null

      val result = repository.registerUser(email, username, password)
      authLoading.value = false

      result.onSuccess { user ->
        currentUser.value = user
        isUserLoggedIn.value = true
        sharedPrefs.edit().putString("active_user_uid", user.uid).apply()
        authSuccessMessage.value = "Account created successfully."
        currentScreen.value = "main"
        selectedTab.value = 0
        onSuccess()
      }.onFailure { error ->
        authError.value = error.message ?: "Failed to register account."
      }
    }
  }

  fun login(
    email: String,
    username: String,
    password: String,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      authLoading.value = true
      authError.value = null
      authSuccessMessage.value = null

      val result = repository.loginUser(email, username, password)
      authLoading.value = false

      result.onSuccess { user ->
        currentUser.value = user
        isUserLoggedIn.value = true
        sharedPrefs.edit().putString("active_user_uid", user.uid).apply()
        authSuccessMessage.value = "Welcome back, ${user.username}!"
        currentScreen.value = "main"
        selectedTab.value = 0
        onSuccess()
      }.onFailure {
        // Enforce strict generic message without revealing which specific field was incorrect
        authError.value = "Email, username or password is incorrect."
      }
    }
  }

  fun requestPasswordReset(
    email: String,
    onResult: (String) -> Unit
  ) {
    viewModelScope.launch {
      authLoading.value = true
      authError.value = null
      val result = repository.requestPasswordReset(email)
      authLoading.value = false

      result.onSuccess { message ->
        onResult(message)
      }.onFailure { error ->
        authError.value = error.message ?: "Please enter a valid email address."
      }
    }
  }

  fun logout() {
    sharedPrefs.edit().remove("active_user_uid").apply()
    currentUser.value = null
    isUserLoggedIn.value = false
    authError.value = null
    authSuccessMessage.value = null
    currentScreen.value = "login"
  }

  fun clearAuthMessages() {
    authError.value = null
    authSuccessMessage.value = null
  }

  // --- AI Assistant State & Actions ---
  private val geminiAiService = com.example.data.ai.GeminiAiService()
  val isAiAssistantOpen = MutableStateFlow(false)
  val isAiThinking = MutableStateFlow(false)

  private val initialAiWelcomeMessage = com.example.data.ai.ChatMessage(
    sender = com.example.data.ai.ChatSender.ASSISTANT,
    text = "Assalam-o-Alaikum! 🌟 Welcome to **Arshad Collection**.\n\nI am your AI Personal Stylist & Shopping Assistant. Ask me anything about:\n• Product recommendations & festive styles\n• Sizing, prices & in-stock availability\n• Delivery times (2-4 business days) & 7-day returns\n• Order tracking & customer support\n\nHow can I help you today?"
  )

  val aiMessages = MutableStateFlow<List<com.example.data.ai.ChatMessage>>(listOf(initialAiWelcomeMessage))

  val aiSuggestedPrompts = listOf(
    "👗 Recommend festive wear under Rs. 4,000",
    "🚚 What are delivery charges & shipping times?",
    "🔄 How does the 7-day return policy work?",
    "👔 Best men's kurta for Eid & events",
    "📦 How can I track my order status?",
    "💬 Contact Arshad Collection on WhatsApp"
  )

  fun openAiAssistant() {
    isAiAssistantOpen.value = true
  }

  fun closeAiAssistant() {
    isAiAssistantOpen.value = false
  }

  fun clearAiChat() {
    aiMessages.value = listOf(initialAiWelcomeMessage)
  }

  fun sendAiUserMessage(userPrompt: String) {
    val prompt = userPrompt.trim()
    if (prompt.isBlank() || isAiThinking.value) return

    val userMsg = com.example.data.ai.ChatMessage(
      sender = com.example.data.ai.ChatSender.USER,
      text = prompt
    )

    aiMessages.value = aiMessages.value + userMsg
    isAiThinking.value = true

    viewModelScope.launch {
      try {
        val currentProducts = allProducts.value
        val currentSettings = appSettings.value
        val userOrders = customerOrders.value.filter { it.userId == currentUser.value?.uid }
        val name = currentUser.value?.username

        val (responseText, recommendedProducts) = geminiAiService.generateResponse(
          userPrompt = prompt,
          chatHistory = aiMessages.value,
          allProducts = currentProducts,
          settings = currentSettings,
          recentOrders = userOrders,
          customerName = name
        )

        val assistantMsg = com.example.data.ai.ChatMessage(
          sender = com.example.data.ai.ChatSender.ASSISTANT,
          text = responseText,
          recommendedProducts = recommendedProducts,
          showWhatsAppButton = responseText.contains("WhatsApp", ignoreCase = true) ||
            prompt.contains("whatsapp", ignoreCase = true) ||
            prompt.contains("contact", ignoreCase = true) ||
            prompt.contains("help", ignoreCase = true)
        )

        aiMessages.value = aiMessages.value + assistantMsg
      } catch (e: Exception) {
        val errorMsg = com.example.data.ai.ChatMessage(
          sender = com.example.data.ai.ChatSender.ASSISTANT,
          text = "I'm having a little trouble fetching the latest update right now. You can chat directly with our WhatsApp care team at 03413399629 for immediate assistance.",
          isError = true,
          showWhatsAppButton = true
        )
        aiMessages.value = aiMessages.value + errorMsg
      } finally {
        isAiThinking.value = false
      }
    }
  }
}


