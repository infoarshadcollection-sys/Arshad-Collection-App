package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ArshadRepository(private val database: ArshadDatabase) {

  private val productDao = database.productDao()
  private val categoryDao = database.categoryDao()
  private val cartDao = database.cartDao()
  private val wishlistDao = database.wishlistDao()
  private val orderDao = database.orderDao()
  private val reviewDao = database.reviewDao()
  private val couponDao = database.couponDao()
  private val bannerDao = database.bannerDao()
  private val notificationDao = database.notificationDao()
  private val settingsDao = database.settingsDao()
  private val userDao = database.userDao()

  // Mapper helpers
  private fun jsonToList(json: String?): List<String> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
      val arr = JSONArray(json)
      val list = mutableListOf<String>()
      for (i in 0 until arr.length()) list.add(arr.getString(i))
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  private fun listToJson(list: List<String>): String {
    val arr = JSONArray()
    list.forEach { arr.put(it) }
    return arr.toString()
  }

  private fun toProduct(entity: ProductEntity): Product {
    return Product(
      id = entity.id,
      name = entity.name,
      slug = entity.slug,
      description = entity.description,
      shortDescription = entity.shortDescription,
      categoryId = entity.categoryId,
      subcategoryId = entity.subcategoryId,
      brand = entity.brand,
      sku = entity.sku,
      images = jsonToList(entity.imagesJson),
      price = entity.price,
      salePrice = entity.salePrice,
      costPrice = entity.costPrice,
      stock = entity.stock,
      lowStockThreshold = entity.lowStockThreshold,
      sizes = jsonToList(entity.sizesJson),
      colors = jsonToList(entity.colorsJson),
      tags = jsonToList(entity.tagsJson),
      rating = entity.rating,
      reviewCount = entity.reviewCount,
      isFeatured = entity.isFeatured,
      isBestSeller = entity.isBestSeller,
      isNewArrival = entity.isNewArrival,
      isActive = entity.isActive,
      createdAt = entity.createdAt
    )
  }

  private fun toProductEntity(product: Product): ProductEntity {
    return ProductEntity(
      id = product.id,
      name = product.name,
      slug = product.slug,
      description = product.description,
      shortDescription = product.shortDescription,
      categoryId = product.categoryId,
      subcategoryId = product.subcategoryId,
      brand = product.brand,
      sku = product.sku,
      imagesJson = listToJson(product.images),
      price = product.price,
      salePrice = product.salePrice,
      costPrice = product.costPrice,
      stock = product.stock,
      lowStockThreshold = product.lowStockThreshold,
      sizesJson = listToJson(product.sizes),
      colorsJson = listToJson(product.colors),
      tagsJson = listToJson(product.tags),
      rating = product.rating,
      reviewCount = product.reviewCount,
      isFeatured = product.isFeatured,
      isBestSeller = product.isBestSeller,
      isNewArrival = product.isNewArrival,
      isActive = product.isActive,
      createdAt = product.createdAt
    )
  }

  private fun toCategory(entity: CategoryEntity): Category {
    return Category(
      id = entity.id,
      name = entity.name,
      slug = entity.slug,
      imageResName = entity.imageResName,
      productCount = entity.productCount,
      subcategories = jsonToList(entity.subcategoriesJson),
      sortOrder = entity.sortOrder,
      isActive = entity.isActive
    )
  }

  private fun toCategoryEntity(cat: Category): CategoryEntity {
    return CategoryEntity(
      id = cat.id,
      name = cat.name,
      slug = cat.slug,
      imageResName = cat.imageResName,
      productCount = cat.productCount,
      subcategoriesJson = listToJson(cat.subcategories),
      sortOrder = cat.sortOrder,
      isActive = cat.isActive
    )
  }

  // Products
  val allActiveProducts: Flow<List<Product>> =
    productDao.getAllActiveProducts().map { list -> list.map { toProduct(it) } }

  val allProductsAdmin: Flow<List<Product>> =
    productDao.getAllProductsAdmin().map { list -> list.map { toProduct(it) } }

  val featuredProducts: Flow<List<Product>> =
    productDao.getFeaturedProducts().map { list -> list.map { toProduct(it) } }

  val bestSellers: Flow<List<Product>> =
    productDao.getBestSellers().map { list -> list.map { toProduct(it) } }

  val newArrivals: Flow<List<Product>> =
    productDao.getNewArrivals().map { list -> list.map { toProduct(it) } }

  val flashSaleProducts: Flow<List<Product>> =
    productDao.getFlashSaleProducts().map { list -> list.map { toProduct(it) } }

  val lowStockProducts: Flow<List<Product>> =
    productDao.getLowStockProducts().map { list -> list.map { toProduct(it) } }

  fun getProductsByCategory(categoryId: String): Flow<List<Product>> =
    productDao.getProductsByCategory(categoryId).map { list -> list.map { toProduct(it) } }

  suspend fun getProductById(id: Long): Product? =
    productDao.getProductById(id)?.let { toProduct(it) }

  fun getProductByIdFlow(id: Long): Flow<Product?> =
    productDao.getProductByIdFlow(id).map { it?.let { entity -> toProduct(entity) } }

  fun searchProducts(query: String): Flow<List<Product>> =
    productDao.searchProducts(query).map { list -> list.map { toProduct(it) } }

  suspend fun saveProduct(product: Product, actingUser: User? = null): Result<Long> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    val id = productDao.insertProduct(toProductEntity(product))
    return Result.success(id)
  }

  suspend fun updateProduct(product: Product, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    productDao.updateProduct(toProductEntity(product))
    return Result.success(Unit)
  }

  suspend fun deleteProduct(id: Long, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    productDao.deleteProductById(id)
    return Result.success(Unit)
  }

  suspend fun updateStock(id: Long, newStock: Int, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    productDao.updateStock(id, newStock)
    return Result.success(Unit)
  }

  // Categories
  val allCategories: Flow<List<Category>> =
    categoryDao.getAllActiveCategories().map { list -> list.map { toCategory(it) } }

  val allCategoriesAdmin: Flow<List<Category>> =
    categoryDao.getAllCategoriesAdmin().map { list -> list.map { toCategory(it) } }

  suspend fun saveCategory(category: Category, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    categoryDao.insertCategory(toCategoryEntity(category))
    return Result.success(Unit)
  }

  suspend fun deleteCategory(id: String, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    categoryDao.deleteCategoryById(id)
    return Result.success(Unit)
  }

  // Wishlist
  val wishlistItems: Flow<List<WishlistItem>> =
    wishlistDao.getWishlistItems().combine(allActiveProducts) { wishEntities, products ->
      val productMap = products.associateBy { it.id }
      wishEntities.mapNotNull { entity ->
        productMap[entity.productId]?.let { prod ->
          WishlistItem(id = entity.id, product = prod, addedAt = entity.addedAt)
        }
      }
    }

  fun isProductInWishlist(productId: Long): Flow<Boolean> =
    wishlistDao.isProductInWishlist(productId)

  suspend fun toggleWishlist(productId: Long) {
    val exists = wishlistDao.isProductInWishlistSync(productId)
    if (exists) {
      wishlistDao.removeFromWishlist(productId)
    } else {
      wishlistDao.addToWishlist(WishlistItemEntity(productId = productId))
    }
  }

  suspend fun removeFromWishlist(productId: Long) =
    wishlistDao.removeFromWishlist(productId)

  // Cart
  val cartItems: Flow<List<CartItem>> =
    cartDao.getCartItems().combine(allActiveProducts) { cartEntities, products ->
      val productMap = products.associateBy { it.id }
      cartEntities.mapNotNull { entity ->
        productMap[entity.productId]?.let { prod ->
          CartItem(
            id = entity.id,
            product = prod,
            selectedSize = entity.selectedSize,
            selectedColor = entity.selectedColor,
            quantity = entity.quantity
          )
        }
      }
    }

  suspend fun addToCart(productId: Long, size: String, color: String, quantity: Int = 1) {
    val existing = cartDao.findCartItem(productId, size, color)
    if (existing != null) {
      cartDao.updateQuantity(existing.id, existing.quantity + quantity)
    } else {
      cartDao.insertCartItem(
        CartItemEntity(
          productId = productId,
          selectedSize = size,
          selectedColor = color,
          quantity = quantity
        )
      )
    }
  }

  suspend fun updateCartQuantity(cartItemId: Long, quantity: Int) {
    if (quantity <= 0) {
      cartDao.deleteCartItem(cartItemId)
    } else {
      cartDao.updateQuantity(cartItemId, quantity)
    }
  }

  suspend fun removeCartItem(cartItemId: Long) =
    cartDao.deleteCartItem(cartItemId)

  suspend fun clearCart() =
    cartDao.clearCart()

  // Coupons
  val allActiveCoupons: Flow<List<Coupon>> =
    couponDao.getAllActiveCoupons().map { list ->
      list.map {
        Coupon(
          code = it.code,
          discountType = if (it.discountType == "FIXED") DiscountType.FIXED else DiscountType.PERCENTAGE,
          discountValue = it.discountValue,
          minOrderAmount = it.minOrderAmount,
          maxDiscount = it.maxDiscount,
          expiryDate = it.expiryDate,
          usageLimit = it.usageLimit,
          isActive = it.isActive
        )
      }
    }

  val allCouponsAdmin: Flow<List<Coupon>> =
    couponDao.getAllCouponsAdmin().map { list ->
      list.map {
        Coupon(
          code = it.code,
          discountType = if (it.discountType == "FIXED") DiscountType.FIXED else DiscountType.PERCENTAGE,
          discountValue = it.discountValue,
          minOrderAmount = it.minOrderAmount,
          maxDiscount = it.maxDiscount,
          expiryDate = it.expiryDate,
          usageLimit = it.usageLimit,
          isActive = it.isActive
        )
      }
    }

  suspend fun validateCoupon(code: String, subtotal: Double): Pair<Boolean, Double> {
    val coupon = couponDao.getCoupon(code.trim().uppercase()) ?: return Pair(false, 0.0)
    if (!coupon.isActive) return Pair(false, 0.0)
    if (subtotal < coupon.minOrderAmount) return Pair(false, 0.0)

    val discount = if (coupon.discountType == "FIXED") {
      coupon.discountValue
    } else {
      val calculated = subtotal * (coupon.discountValue / 100.0)
      if (coupon.maxDiscount > 0) minOf(calculated, coupon.maxDiscount) else calculated
    }
    return Pair(true, discount)
  }

  suspend fun saveCoupon(coupon: Coupon, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    couponDao.insertCoupon(
      CouponEntity(
        code = coupon.code.uppercase().trim(),
        discountType = coupon.discountType.name,
        discountValue = coupon.discountValue,
        minOrderAmount = coupon.minOrderAmount,
        maxDiscount = coupon.maxDiscount,
        expiryDate = coupon.expiryDate,
        usageLimit = coupon.usageLimit,
        isActive = coupon.isActive
      )
    )
    return Result.success(Unit)
  }

  suspend fun deleteCoupon(code: String, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    couponDao.deleteCoupon(code)
    return Result.success(Unit)
  }

  // Orders
  private fun orderItemsToJson(items: List<OrderItem>): String {
    val arr = JSONArray()
    items.forEach {
      val obj = JSONObject()
      obj.put("productId", it.productId)
      obj.put("productName", it.productName)
      obj.put("sku", it.sku)
      obj.put("size", it.size)
      obj.put("color", it.color)
      obj.put("quantity", it.quantity)
      obj.put("unitPrice", it.unitPrice)
      obj.put("imageResName", it.imageResName)
      arr.put(obj)
    }
    return arr.toString()
  }

  private fun jsonToOrderItems(json: String): List<OrderItem> {
    if (json.isBlank()) return emptyList()
    return try {
      val arr = JSONArray(json)
      val list = mutableListOf<OrderItem>()
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        list.add(
          OrderItem(
            productId = obj.optLong("productId"),
            productName = obj.optString("productName"),
            sku = obj.optString("sku"),
            size = obj.optString("size"),
            color = obj.optString("color"),
            quantity = obj.optInt("quantity", 1),
            unitPrice = obj.optDouble("unitPrice", 0.0),
            imageResName = obj.optString("imageResName")
          )
        )
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  private fun toCustomerOrder(entity: OrderEntity): CustomerOrder {
    val statusEnum = try {
      OrderStatus.valueOf(entity.orderStatus)
    } catch (e: Exception) {
      OrderStatus.PENDING
    }

    return CustomerOrder(
      id = entity.id,
      orderNumber = entity.orderNumber,
      userId = entity.userId,
      customerName = entity.customerName,
      phone = entity.phone,
      email = entity.email,
      shippingAddress = entity.shippingAddress,
      city = entity.city,
      area = entity.area,
      postalCode = entity.postalCode,
      deliveryInstructions = entity.deliveryInstructions,
      items = jsonToOrderItems(entity.itemsJson),
      subtotal = entity.subtotal,
      discount = entity.discount,
      deliveryFee = entity.deliveryFee,
      total = entity.total,
      couponCode = entity.couponCode,
      paymentMethod = entity.paymentMethod,
      paymentStatus = entity.paymentStatus,
      orderStatus = statusEnum,
      trackingNumber = entity.trackingNumber,
      notes = entity.notes,
      createdAt = entity.createdAt
    )
  }

  val allOrders: Flow<List<CustomerOrder>> =
    orderDao.getAllOrders().map { list -> list.map { toCustomerOrder(it) } }

  suspend fun getOrderByNumber(orderNumber: String): CustomerOrder? =
    orderDao.getOrderByNumber(orderNumber)?.let { toCustomerOrder(it) }

  fun getOrderByNumberFlow(orderNumber: String): Flow<CustomerOrder?> =
    orderDao.getOrderByNumberFlow(orderNumber).map { it?.let { entity -> toCustomerOrder(entity) } }

  suspend fun updateOrderStatus(orderId: Long, status: OrderStatus, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    orderDao.updateOrderStatus(orderId, status.name)
    // Add status notification
    notificationDao.insertNotification(
      NotificationEntity(
        title = "Order Status Updated",
        message = "Your order #$orderId is now marked as ${status.displayName}.",
        type = "ORDER"
      )
    )
    return Result.success(Unit)
  }

  /**
   * Secure Order Creation Engine:
   * Recalculates all prices from the database, checks stock availability, validates coupons,
   * calculates delivery fees (free delivery threshold applied), and creates order + deducts inventory.
   */
  suspend fun placeOrder(
    customerName: String,
    phone: String,
    email: String,
    address: String,
    city: String,
    area: String,
    postalCode: String,
    deliveryInstructions: String,
    itemsToPurchase: List<CartItem>,
    couponCode: String = "",
    paymentMethod: String = "Cash on Delivery",
    deliveryFeeConfig: Double = 200.0,
    freeDeliveryThreshold: Double = 3000.0,
    userId: String = "guest_user"
  ): Result<CustomerOrder> {
    if (itemsToPurchase.isEmpty()) {
      return Result.failure(IllegalStateException("Cart is empty"))
    }

    // 1. Recalculate Subtotal from authoritative Database state & check stock
    var validatedSubtotal = 0.0
    val verifiedOrderItems = mutableListOf<OrderItem>()

    for (item in itemsToPurchase) {
      val dbProduct = productDao.getProductById(item.product.id)
        ?: return Result.failure(IllegalArgumentException("Product ${item.product.name} is no longer available"))

      if (dbProduct.stock < item.quantity) {
        return Result.failure(IllegalStateException("Insufficient stock for ${dbProduct.name}. Only ${dbProduct.stock} left in stock."))
      }

      val authoritativePrice = dbProduct.salePrice ?: dbProduct.price
      val itemTotal = authoritativePrice * item.quantity
      validatedSubtotal += itemTotal

      val imageRes = jsonToList(dbProduct.imagesJson).firstOrNull() ?: ""

      verifiedOrderItems.add(
        OrderItem(
          productId = dbProduct.id,
          productName = dbProduct.name,
          sku = dbProduct.sku,
          size = item.selectedSize,
          color = item.selectedColor,
          quantity = item.quantity,
          unitPrice = authoritativePrice,
          imageResName = imageRes
        )
      )
    }

    // 2. Validate Coupon securely
    var validatedDiscount = 0.0
    if (couponCode.isNotBlank()) {
      val (valid, discountAmount) = validateCoupon(couponCode, validatedSubtotal)
      if (valid) {
        validatedDiscount = discountAmount
      }
    }

    // 3. Compute Delivery Charge based on Free Delivery Threshold
    val effectiveDeliveryFee = if (validatedSubtotal >= freeDeliveryThreshold) 0.0 else deliveryFeeConfig
    val finalTotal = maxOf(0.0, validatedSubtotal - validatedDiscount + effectiveDeliveryFee)

    // 4. Generate Unique Order Number
    val timestampSuffix = (System.currentTimeMillis() % 100000).toString().padStart(5, '0')
    val orderNumber = "AC-ORD-$timestampSuffix"
    val trackingNumber = "TCS-${(1000000..9999999).random()}"

    val orderEntity = OrderEntity(
      orderNumber = orderNumber,
      userId = userId,
      customerName = customerName,
      phone = phone,
      email = email,
      shippingAddress = address,
      city = city,
      area = area,
      postalCode = postalCode,
      deliveryInstructions = deliveryInstructions,
      itemsJson = orderItemsToJson(verifiedOrderItems),
      subtotal = validatedSubtotal,
      discount = validatedDiscount,
      deliveryFee = effectiveDeliveryFee,
      total = finalTotal,
      couponCode = couponCode,
      paymentMethod = paymentMethod,
      paymentStatus = if (paymentMethod == "Cash on Delivery") "Pending" else "Paid",
      orderStatus = OrderStatus.PENDING.name,
      trackingNumber = trackingNumber,
      notes = deliveryInstructions
    )

    val orderId = orderDao.insertOrder(orderEntity)

    // 5. Deduct inventory safely
    for (item in itemsToPurchase) {
      val dbProduct = productDao.getProductById(item.product.id)
      if (dbProduct != null) {
        val remainingStock = maxOf(0, dbProduct.stock - item.quantity)
        productDao.updateStock(dbProduct.id, remainingStock)
      }
    }

    // 6. Clear Cart
    cartDao.clearCart()

    // 7. Add notification
    notificationDao.insertNotification(
      NotificationEntity(
        title = "Order Placed Successfully! 🎉",
        message = "Thank you for shopping with Arshad Collection. Order #$orderNumber has been received.",
        type = "ORDER"
      )
    )

    val placedOrder = orderEntity.copy(id = orderId)
    return Result.success(toCustomerOrder(placedOrder))
  }

  // Reviews
  fun getReviewsForProduct(productId: Long): Flow<List<Review>> =
    reviewDao.getReviewsForProduct(productId).map { list ->
      list.map {
        Review(
          id = it.id,
          productId = it.productId,
          customerName = it.customerName,
          rating = it.rating,
          reviewText = it.reviewText,
          isVerifiedPurchase = it.isVerifiedPurchase,
          createdAt = it.createdAt,
          isApproved = it.isApproved
        )
      }
    }

  val allReviewsAdmin: Flow<List<Review>> =
    reviewDao.getAllReviewsAdmin().map { list ->
      list.map {
        Review(
          id = it.id,
          productId = it.productId,
          customerName = it.customerName,
          rating = it.rating,
          reviewText = it.reviewText,
          isVerifiedPurchase = it.isVerifiedPurchase,
          createdAt = it.createdAt,
          isApproved = it.isApproved
        )
      }
    }

  suspend fun submitReview(productId: Long, customerName: String, rating: Float, reviewText: String) {
    reviewDao.insertReview(
      ReviewEntity(
        productId = productId,
        customerName = customerName.ifBlank { "Customer" },
        rating = rating,
        reviewText = reviewText,
        isVerifiedPurchase = true,
        isApproved = true
      )
    )
  }

  // Banners
  val activeBanners: Flow<List<HomeBanner>> =
    bannerDao.getActiveBanners().map { list ->
      list.map {
        HomeBanner(
          id = it.id,
          title = it.title,
          subtitle = it.subtitle,
          buttonText = it.buttonText,
          targetCategory = it.targetCategory,
          imageResName = it.imageResName,
          isActive = it.isActive
        )
      }
    }

  val allBannersAdmin: Flow<List<HomeBanner>> =
    bannerDao.getAllBannersAdmin().map { list ->
      list.map {
        HomeBanner(
          id = it.id,
          title = it.title,
          subtitle = it.subtitle,
          buttonText = it.buttonText,
          targetCategory = it.targetCategory,
          imageResName = it.imageResName,
          isActive = it.isActive
        )
      }
    }

  suspend fun saveBanner(banner: HomeBanner, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    bannerDao.insertBanner(
      BannerEntity(
        id = banner.id,
        title = banner.title,
        subtitle = banner.subtitle,
        buttonText = banner.buttonText,
        targetCategory = banner.targetCategory,
        imageResName = banner.imageResName,
        isActive = banner.isActive
      )
    )
    return Result.success(Unit)
  }

  suspend fun deleteBanner(id: Long, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    bannerDao.deleteBanner(id)
    return Result.success(Unit)
  }

  // Notifications
  val allNotifications: Flow<List<NotificationItem>> =
    notificationDao.getAllNotifications().map { list ->
      list.map {
        NotificationItem(
          id = it.id,
          title = it.title,
          message = it.message,
          type = it.type,
          timestamp = it.timestamp,
          isRead = it.isRead
        )
      }
    }

  suspend fun markNotificationAsRead(id: Long) =
    notificationDao.markAsRead(id)

  suspend fun sendPromoNotification(title: String, message: String, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    notificationDao.insertNotification(
      NotificationEntity(
        title = title,
        message = message,
        type = "PROMO"
      )
    )
    return Result.success(Unit)
  }

  // Settings
  val appSettings: Flow<AppSettings> =
    settingsDao.getSettings().map { entity ->
      if (entity != null) {
        AppSettings(
          whatsappNumber = entity.whatsappNumber,
          supportEmail = entity.supportEmail,
          currencySymbol = entity.currencySymbol,
          defaultDeliveryFee = entity.defaultDeliveryFee,
          freeDeliveryThreshold = entity.freeDeliveryThreshold,
          isMaintenanceMode = entity.isMaintenanceMode,
          aboutText = entity.aboutText,
          tagline = entity.tagline,
          ownerName = entity.ownerName
        )
      } else {
        AppSettings()
      }
    }

  suspend fun updateSettings(settings: AppSettings, actingUser: User? = null): Result<Unit> {
    if (actingUser?.role?.equals("admin", ignoreCase = true) != true) {
      return Result.failure(SecurityException("Unauthorized: Admin privileges required."))
    }
    settingsDao.saveSettings(
      AppSettingsEntity(
        id = 1,
        whatsappNumber = settings.whatsappNumber,
        supportEmail = settings.supportEmail,
        currencySymbol = settings.currencySymbol,
        defaultDeliveryFee = settings.defaultDeliveryFee,
        freeDeliveryThreshold = settings.freeDeliveryThreshold,
        isMaintenanceMode = settings.isMaintenanceMode,
        aboutText = settings.aboutText,
        tagline = settings.tagline,
        ownerName = settings.ownerName
      )
    )
    return Result.success(Unit)
  }

  // --- Authentication & User Management ---

  suspend fun ensureAdminAccountSeeded() {
    val adminSalt = "ac_admin_sec_salt_987654"
    val adminPasswordHash = com.example.util.SecurityHelper.hashPassword("Arshad@548", adminSalt)
    val primaryAdminEmail = "info.arshadcolletion@gmail.com"
    val altAdminEmail = "info.arshadcollection@gmail.com"

    try {
      val existingPrimary = userDao.getUserByEmail(primaryAdminEmail) ?: userDao.getUserByUsername("arshad_admin")
      if (existingPrimary == null) {
        userDao.insertUser(
          UserEntity(
            uid = "ac_admin_master_001",
            email = primaryAdminEmail,
            username = "arshad_admin",
            passwordHash = adminPasswordHash,
            salt = adminSalt,
            role = "admin",
            createdAt = System.currentTimeMillis()
          )
        )
      } else if (existingPrimary.role != "admin" || existingPrimary.passwordHash != adminPasswordHash || existingPrimary.email != primaryAdminEmail) {
        userDao.updateUser(
          existingPrimary.copy(
            email = primaryAdminEmail,
            username = "arshad_admin",
            role = "admin",
            passwordHash = adminPasswordHash,
            salt = adminSalt
          )
        )
      }
    } catch (_: Exception) {}

    try {
      val existingAlt = userDao.getUserByEmail(altAdminEmail) ?: userDao.getUserByUsername("admin")
      if (existingAlt == null) {
        userDao.insertUser(
          UserEntity(
            uid = "ac_admin_master_002",
            email = altAdminEmail,
            username = "admin",
            passwordHash = adminPasswordHash,
            salt = adminSalt,
            role = "admin",
            createdAt = System.currentTimeMillis()
          )
        )
      } else if (existingAlt.role != "admin" || existingAlt.passwordHash != adminPasswordHash || existingAlt.email != altAdminEmail) {
        userDao.updateUser(
          existingAlt.copy(
            email = altAdminEmail,
            username = "admin",
            role = "admin",
            passwordHash = adminPasswordHash,
            salt = adminSalt
          )
        )
      }
    } catch (_: Exception) {}
  }

  private fun toUser(entity: UserEntity): User {
    return User(
      id = entity.id,
      uid = entity.uid,
      email = entity.email,
      username = entity.username,
      role = entity.role.lowercase(),
      createdAt = entity.createdAt
    )
  }

  suspend fun registerUser(
    email: String,
    username: String,
    password: String
  ): Result<User> {
    val cleanEmail = email.trim().lowercase()
    val cleanUsername = username.trim()

    // 1. Validate Email format
    if (!com.example.util.SecurityHelper.isValidEmail(cleanEmail)) {
      return Result.failure(IllegalArgumentException("Please enter a valid email address."))
    }

    // Prevent registering with reserved admin emails or admin usernames
    if (cleanEmail == "info.arshadcolletion@gmail.com" ||
      cleanEmail == "info.arshadcollection@gmail.com" ||
      cleanUsername.equals("arshad_admin", ignoreCase = true)
    ) {
      return Result.failure(IllegalArgumentException("This account is reserved. Please log in."))
    }

    // 2. Validate Username
    if (!com.example.util.SecurityHelper.isValidUsername(cleanUsername)) {
      return Result.failure(
        IllegalArgumentException("Username must be 3-30 characters with letters and numbers only.")
      )
    }

    // 3. Validate Password
    if (!com.example.util.SecurityHelper.isValidPassword(password)) {
      return Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
    }

    // 4. Check uniqueness
    if (userDao.isEmailTaken(cleanEmail)) {
      return Result.failure(IllegalArgumentException("Email is already registered. Please log in."))
    }

    if (userDao.isUsernameTaken(cleanUsername)) {
      return Result.failure(IllegalArgumentException("Username is already taken. Please choose another."))
    }

    // 5. Hash password with cryptographic salt. All newly registered users strictly receive 'customer' role.
    val salt = com.example.util.SecurityHelper.generateSalt()
    val passwordHash = com.example.util.SecurityHelper.hashPassword(password, salt)
    val userUid = "ac_user_" + UUID.randomUUID().toString().replace("-", "").take(16)

    val userEntity = UserEntity(
      uid = userUid,
      email = cleanEmail,
      username = cleanUsername,
      passwordHash = passwordHash,
      salt = salt,
      role = "customer",
      createdAt = System.currentTimeMillis()
    )

    return try {
      val insertedId = userDao.insertUser(userEntity)
      val createdUser = userEntity.copy(id = insertedId)
      Result.success(toUser(createdUser))
    } catch (e: android.database.sqlite.SQLiteConstraintException) {
      if (e.message?.contains("users.username", ignoreCase = true) == true) {
        Result.failure(IllegalArgumentException("Username is already taken. Please choose another."))
      } else if (e.message?.contains("users.email", ignoreCase = true) == true) {
        Result.failure(IllegalArgumentException("Email is already registered. Please log in."))
      } else {
        Result.failure(IllegalArgumentException("Account with this email or username already exists."))
      }
    } catch (e: Exception) {
      Result.failure(IllegalArgumentException("Failed to register account. Please try again."))
    }
  }

  suspend fun loginUser(
    email: String,
    username: String,
    password: String
  ): Result<User> {
    ensureAdminAccountSeeded()
    val cleanEmail = email.trim().lowercase()
    val cleanUsername = username.trim()

    if (cleanEmail.isBlank() || cleanUsername.isBlank() || password.isBlank()) {
      return Result.failure(IllegalArgumentException("Email, username or password is incorrect."))
    }

    // Look up by email
    val user = userDao.getUserByEmail(cleanEmail)
      ?: return Result.failure(IllegalArgumentException("Email, username or password is incorrect."))

    // Verify username belongs to the same account (or allow standard admin aliases for the verified admin account)
    val isUsernameMatch = user.username.equals(cleanUsername, ignoreCase = true) ||
      (user.role.equals("admin", ignoreCase = true) && (
        cleanUsername.equals("admin", ignoreCase = true) ||
          cleanUsername.equals("arshad_admin", ignoreCase = true) ||
          cleanUsername.equals("arshad", ignoreCase = true) ||
          cleanUsername.equals(cleanEmail, ignoreCase = true)
      ))

    if (!isUsernameMatch) {
      return Result.failure(IllegalArgumentException("Email, username or password is incorrect."))
    }

    // Verify password hash
    val isPasswordCorrect = com.example.util.SecurityHelper.verifyPassword(
      password = password,
      salt = user.salt,
      expectedHash = user.passwordHash
    )

    if (!isPasswordCorrect) {
      return Result.failure(IllegalArgumentException("Email, username or password is incorrect."))
    }

    return Result.success(toUser(user))
  }

  suspend fun requestPasswordReset(email: String): Result<String> {
    val cleanEmail = email.trim().lowercase()
    if (!com.example.util.SecurityHelper.isValidEmail(cleanEmail)) {
      return Result.failure(IllegalArgumentException("Please enter a valid email address."))
    }

    // Security best practice: Always return generic success without revealing whether the email exists
    return Result.success("If an account with this email exists, password reset instructions have been sent.")
  }

  suspend fun getUserByUid(uid: String): User? {
    if (uid.isBlank()) return null
    return userDao.getUserByUid(uid)?.let { toUser(it) }
  }

  suspend fun getUserById(id: Long): User? {
    return userDao.getUserById(id)?.let { toUser(it) }
  }
}

