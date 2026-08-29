package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
  @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY id DESC")
  fun getAllActiveProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products ORDER BY id DESC")
  fun getAllProductsAdmin(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE id = :id")
  suspend fun getProductById(id: Long): ProductEntity?

  @Query("SELECT * FROM products WHERE id = :id")
  fun getProductByIdFlow(id: Long): Flow<ProductEntity?>

  @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isActive = 1")
  fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isFeatured = 1 AND isActive = 1")
  fun getFeaturedProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isBestSeller = 1 AND isActive = 1")
  fun getBestSellers(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isNewArrival = 1 AND isActive = 1")
  fun getNewArrivals(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE salePrice IS NOT NULL AND salePrice < price AND isActive = 1")
  fun getFlashSaleProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE stock <= lowStockThreshold AND isActive = 1")
  fun getLowStockProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%') AND isActive = 1")
  fun searchProducts(query: String): Flow<List<ProductEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(products: List<ProductEntity>)

  @Update
  suspend fun updateProduct(product: ProductEntity)

  @Query("UPDATE products SET stock = :newStock WHERE id = :id")
  suspend fun updateStock(id: Long, newStock: Int)

  @Delete
  suspend fun deleteProduct(product: ProductEntity)

  @Query("DELETE FROM products WHERE id = :id")
  suspend fun deleteProductById(id: Long)
}

@Dao
interface CategoryDao {
  @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder ASC")
  fun getAllActiveCategories(): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
  fun getAllCategoriesAdmin(): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories WHERE id = :id")
  suspend fun getCategoryById(id: String): CategoryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(categories: List<CategoryEntity>)

  @Update
  suspend fun updateCategory(category: CategoryEntity)

  @Query("DELETE FROM categories WHERE id = :id")
  suspend fun deleteCategoryById(id: String)
}

@Dao
interface CartDao {
  @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
  fun getCartItems(): Flow<List<CartItemEntity>>

  @Query("SELECT * FROM cart_items WHERE productId = :productId AND selectedSize = :size AND selectedColor = :color LIMIT 1")
  suspend fun findCartItem(productId: Long, size: String, color: String): CartItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCartItem(item: CartItemEntity)

  @Update
  suspend fun updateCartItem(item: CartItemEntity)

  @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
  suspend fun updateQuantity(id: Long, quantity: Int)

  @Query("DELETE FROM cart_items WHERE id = :id")
  suspend fun deleteCartItem(id: Long)

  @Query("DELETE FROM cart_items")
  suspend fun clearCart()
}

@Dao
interface WishlistDao {
  @Query("SELECT * FROM wishlist_items ORDER BY addedAt DESC")
  fun getWishlistItems(): Flow<List<WishlistItemEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId)")
  fun isProductInWishlist(productId: Long): Flow<Boolean>

  @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId)")
  suspend fun isProductInWishlistSync(productId: Long): Boolean

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addToWishlist(item: WishlistItemEntity)

  @Query("DELETE FROM wishlist_items WHERE productId = :productId")
  suspend fun removeFromWishlist(productId: Long)
}

@Dao
interface OrderDao {
  @Query("SELECT * FROM orders ORDER BY createdAt DESC")
  fun getAllOrders(): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE id = :id")
  suspend fun getOrderById(id: Long): OrderEntity?

  @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
  suspend fun getOrderByNumber(orderNumber: String): OrderEntity?

  @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
  fun getOrderByNumberFlow(orderNumber: String): Flow<OrderEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity): Long

  @Update
  suspend fun updateOrder(order: OrderEntity)

  @Query("UPDATE orders SET orderStatus = :status WHERE id = :id")
  suspend fun updateOrderStatus(id: Long, status: String)

  @Query("SELECT COUNT(*) FROM orders")
  fun getOrderCount(): Flow<Int>
}

@Dao
interface ReviewDao {
  @Query("SELECT * FROM reviews WHERE productId = :productId AND isApproved = 1 ORDER BY createdAt DESC")
  fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>>

  @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
  fun getAllReviewsAdmin(): Flow<List<ReviewEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReview(review: ReviewEntity)

  @Query("UPDATE reviews SET isApproved = :approved WHERE id = :id")
  suspend fun updateReviewApproval(id: Long, approved: Boolean)

  @Query("DELETE FROM reviews WHERE id = :id")
  suspend fun deleteReview(id: Long)
}

@Dao
interface CouponDao {
  @Query("SELECT * FROM coupons WHERE isActive = 1")
  fun getAllActiveCoupons(): Flow<List<CouponEntity>>

  @Query("SELECT * FROM coupons ORDER BY code ASC")
  fun getAllCouponsAdmin(): Flow<List<CouponEntity>>

  @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
  suspend fun getCoupon(code: String): CouponEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCoupon(coupon: CouponEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(coupons: List<CouponEntity>)

  @Update
  suspend fun updateCoupon(coupon: CouponEntity)

  @Query("DELETE FROM coupons WHERE code = :code")
  suspend fun deleteCoupon(code: String)
}

@Dao
interface BannerDao {
  @Query("SELECT * FROM banners WHERE isActive = 1 ORDER BY id ASC")
  fun getActiveBanners(): Flow<List<BannerEntity>>

  @Query("SELECT * FROM banners ORDER BY id ASC")
  fun getAllBannersAdmin(): Flow<List<BannerEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBanner(banner: BannerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(banners: List<BannerEntity>)

  @Update
  suspend fun updateBanner(banner: BannerEntity)

  @Query("DELETE FROM banners WHERE id = :id")
  suspend fun deleteBanner(id: Long)
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<NotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: Long)

  @Query("DELETE FROM notifications")
  suspend fun clearAll()
}

@Dao
interface SettingsDao {
  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<AppSettingsEntity?>

  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  suspend fun getSettingsSync(): AppSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveSettings(settings: AppSettingsEntity)
}

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(:username)) LIMIT 1")
  suspend fun getUserByUsername(username: String): UserEntity?

  @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
  suspend fun getUserByUid(uid: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun getUserById(id: Long): UserEntity?

  @Query("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)))")
  suspend fun isEmailTaken(email: String): Boolean

  @Query("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(:username)))")
  suspend fun isUsernameTaken(username: String): Boolean

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertUser(user: UserEntity): Long

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET passwordHash = :passwordHash, salt = :salt WHERE id = :userId")
  suspend fun updatePassword(userId: Long, passwordHash: String, salt: String)

  @Query("SELECT COUNT(*) FROM users")
  suspend fun getUserCount(): Int
}

