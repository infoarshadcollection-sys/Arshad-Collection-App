package com.example.data.model

data class Product(
  val id: Long = 0,
  val name: String,
  val slug: String,
  val description: String,
  val shortDescription: String,
  val categoryId: String,
  val subcategoryId: String = "",
  val brand: String = "Arshad Collection",
  val sku: String,
  val images: List<String> = emptyList(),
  val price: Double,
  val salePrice: Double? = null,
  val costPrice: Double? = null,
  val stock: Int = 10,
  val lowStockThreshold: Int = 3,
  val sizes: List<String> = emptyList(),
  val colors: List<String> = emptyList(),
  val tags: List<String> = emptyList(),
  val rating: Float = 4.8f,
  val reviewCount: Int = 12,
  val isFeatured: Boolean = false,
  val isBestSeller: Boolean = false,
  val isNewArrival: Boolean = false,
  val isActive: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
) {
  val currentPrice: Double
    get() = salePrice ?: price

  val discountPercent: Int
    get() {
      val sale = salePrice ?: return 0
      if (price <= 0 || sale >= price) return 0
      return (((price - sale) / price) * 100).toInt()
    }

  val isInStock: Boolean
    get() = stock > 0

  val isLowStock: Boolean
    get() = stock in 1..lowStockThreshold
}

data class Category(
  val id: String,
  val name: String,
  val slug: String,
  val imageResName: String,
  val productCount: Int = 0,
  val subcategories: List<String> = emptyList(),
  val sortOrder: Int = 0,
  val isActive: Boolean = true
)

data class CartItem(
  val id: Long = 0,
  val product: Product,
  val selectedSize: String = "",
  val selectedColor: String = "",
  val quantity: Int = 1
) {
  val itemTotal: Double
    get() = product.currentPrice * quantity
}

data class WishlistItem(
  val id: Long = 0,
  val product: Product,
  val addedAt: Long = System.currentTimeMillis()
)

data class OrderItem(
  val productId: Long,
  val productName: String,
  val sku: String,
  val size: String = "",
  val color: String = "",
  val quantity: Int,
  val unitPrice: Double,
  val imageResName: String = ""
) {
  val totalPrice: Double
    get() = unitPrice * quantity
}

enum class OrderStatus(val displayName: String, val stepIndex: Int) {
  PENDING("Pending", 0),
  CONFIRMED("Confirmed", 1),
  PROCESSING("Processing", 2),
  PACKED("Packed", 3),
  SHIPPED("Shipped", 4),
  OUT_FOR_DELIVERY("Out for Delivery", 5),
  DELIVERED("Delivered", 6),
  CANCELLED("Cancelled", -1),
  RETURNED("Returned", -2)
}

data class CustomerOrder(
  val id: Long = 0,
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
  val items: List<OrderItem>,
  val subtotal: Double,
  val discount: Double = 0.0,
  val deliveryFee: Double = 200.0,
  val total: Double,
  val couponCode: String = "",
  val paymentMethod: String = "Cash on Delivery",
  val paymentStatus: String = "Pending",
  val orderStatus: OrderStatus = OrderStatus.PENDING,
  val trackingNumber: String = "",
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

data class Review(
  val id: Long = 0,
  val productId: Long,
  val customerName: String,
  val rating: Float,
  val reviewText: String,
  val isVerifiedPurchase: Boolean = true,
  val createdAt: Long = System.currentTimeMillis(),
  val isApproved: Boolean = true
)

enum class DiscountType {
  PERCENTAGE, FIXED
}

data class Coupon(
  val code: String,
  val discountType: DiscountType,
  val discountValue: Double,
  val minOrderAmount: Double = 0.0,
  val maxDiscount: Double = 0.0,
  val expiryDate: String = "31 Dec 2026",
  val usageLimit: Int = 100,
  val isActive: Boolean = true
)

data class HomeBanner(
  val id: Long = 0,
  val title: String,
  val subtitle: String,
  val buttonText: String,
  val targetCategory: String,
  val imageResName: String,
  val isActive: Boolean = true
)

data class NotificationItem(
  val id: Long = 0,
  val title: String,
  val message: String,
  val type: String = "PROMO", // ORDER, PROMO, SYSTEM
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)

data class AppSettings(
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

data class User(
  val id: Long = 0,
  val uid: String = "",
  val email: String = "",
  val username: String = "",
  val role: String = "CUSTOMER",
  val createdAt: Long = System.currentTimeMillis()
)

