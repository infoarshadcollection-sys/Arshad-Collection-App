package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val slug: String,
  val description: String,
  val shortDescription: String,
  val categoryId: String,
  val subcategoryId: String = "",
  val brand: String = "Arshad Collection",
  val sku: String,
  val imagesJson: String = "[]",
  val price: Double,
  val salePrice: Double? = null,
  val costPrice: Double? = null,
  val stock: Int = 10,
  val lowStockThreshold: Int = 3,
  val sizesJson: String = "[]",
  val colorsJson: String = "[]",
  val tagsJson: String = "[]",
  val rating: Float = 4.8f,
  val reviewCount: Int = 12,
  val isFeatured: Boolean = false,
  val isBestSeller: Boolean = false,
  val isNewArrival: Boolean = false,
  val isActive: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey val id: String,
  val name: String,
  val slug: String,
  val imageResName: String,
  val productCount: Int = 0,
  val subcategoriesJson: String = "[]",
  val sortOrder: Int = 0,
  val isActive: Boolean = true
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val productId: Long,
  val selectedSize: String = "",
  val selectedColor: String = "",
  val quantity: Int = 1,
  val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val productId: Long,
  val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val orderNumber: String,
  val userId: String = "guest_user",
  val customerName: String,
  val phone: String,
  val email: String = "",
  val shippingAddress: String,
  val city: String,
  val area: String,
  val postalCode: String = "",
  val deliveryInstructions: String = "",
  val itemsJson: String = "[]",
  val subtotal: Double,
  val discount: Double = 0.0,
  val deliveryFee: Double = 200.0,
  val total: Double,
  val couponCode: String = "",
  val paymentMethod: String = "Cash on Delivery",
  val paymentStatus: String = "Pending",
  val orderStatus: String = "PENDING",
  val trackingNumber: String = "",
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val productId: Long,
  val customerName: String,
  val rating: Float,
  val reviewText: String,
  val isVerifiedPurchase: Boolean = true,
  val createdAt: Long = System.currentTimeMillis(),
  val isApproved: Boolean = true
)

@Entity(tableName = "coupons")
data class CouponEntity(
  @PrimaryKey val code: String,
  val discountType: String = "PERCENTAGE", // PERCENTAGE or FIXED
  val discountValue: Double,
  val minOrderAmount: Double = 0.0,
  val maxDiscount: Double = 0.0,
  val expiryDate: String = "31 Dec 2026",
  val usageLimit: Int = 100,
  val isActive: Boolean = true
)

@Entity(tableName = "banners")
data class BannerEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val subtitle: String,
  val buttonText: String,
  val targetCategory: String,
  val imageResName: String,
  val isActive: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val message: String,
  val type: String = "PROMO",
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val whatsappNumber: String = "03413399629",
  val supportEmail: String = "info.arshadcollection@gmail.com",
  val currencySymbol: String = "Rs.",
  val defaultDeliveryFee: Double = 200.0,
  val freeDeliveryThreshold: Double = 3000.0,
  val isMaintenanceMode: Boolean = false,
  val aboutText: String = "Arshad Collection is an online retail store focused on bringing customers quality products across fashion, home textiles, lifestyle, beauty, gadgets, cultural products and more.",
  val tagline: String = "Style • Quality • Trust",
  val ownerName: String = "Arshad Ahmed"
)

@Entity(
  tableName = "users",
  indices = [
    androidx.room.Index(value = ["email"], unique = true),
    androidx.room.Index(value = ["username"], unique = true)
  ]
)
data class UserEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val uid: String,
  val email: String,
  val username: String,
  val passwordHash: String,
  val salt: String,
  val role: String = "CUSTOMER",
  val createdAt: Long = System.currentTimeMillis()
)

