package com.example.data.supabase

import android.util.Log
import com.example.data.model.Category
import com.example.data.model.CustomerOrder
import com.example.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class SupabaseClient(private val config: SupabaseConfig) {

  companion object {
    private const val TAG = "SupabaseClient"
  }

  private val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .build()

  @Volatile
  var schemaInfo: SupabaseSchemaInfo = SupabaseSchemaInfo()
    private set

  /**
   * Inspects the Supabase OpenAPI documentation at /rest/v1/
   * to automatically detect tables, columns, and relationships.
   */
  suspend fun inspectDatabaseSchema(): Result<SupabaseSchemaInfo> = withContext(Dispatchers.IO) {
    if (!config.isConfigured()) {
      return@withContext Result.failure(IllegalStateException("Supabase is not configured yet."))
    }

    val url = config.getSupabaseUrl()
    val anonKey = config.getAnonKey()

    try {
      val request = Request.Builder()
        .url("$url/rest/v1/")
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .addHeader("Accept", "application/json")
        .build()

      val response = httpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        val errBody = response.body?.string().orEmpty()
        Log.w(TAG, "Schema inspection returned HTTP ${response.code}: $errBody")
        // Default to standard tables if OpenAPI is restricted by RLS
        val fallback = SupabaseSchemaInfo(
          detectedProductsTable = "products",
          detectedCategoriesTable = "categories",
          isSchemaInspected = true
        )
        schemaInfo = fallback
        return@withContext Result.success(fallback)
      }

      val jsonStr = response.body?.string().orEmpty()
      val jsonObj = JSONObject(jsonStr)

      var detectedProducts = "products"
      var detectedCategories = "categories"
      val columns = mutableListOf<String>()

      // Check OpenAPI definitions or paths
      if (jsonObj.has("definitions")) {
        val defs = jsonObj.getJSONObject("definitions")
        if (defs.has("products")) detectedProducts = "products"
        else if (defs.has("product")) detectedProducts = "product"
        else if (defs.has("items")) detectedProducts = "items"

        if (defs.has("categories")) detectedCategories = "categories"
        else if (defs.has("category")) detectedCategories = "category"

        // Inspect columns of the detected product table
        if (defs.has(detectedProducts)) {
          val prodDef = defs.getJSONObject(detectedProducts)
          if (prodDef.has("properties")) {
            val props = prodDef.getJSONObject("properties")
            val keys = props.keys()
            while (keys.hasNext()) {
              columns.add(keys.next())
            }
          }
        }
      } else if (jsonObj.has("paths")) {
        val paths = jsonObj.getJSONObject("paths")
        if (paths.has("/products")) detectedProducts = "products"
        if (paths.has("/categories")) detectedCategories = "categories"
      }

      val info = SupabaseSchemaInfo(
        detectedProductsTable = detectedProducts,
        detectedCategoriesTable = detectedCategories,
        detectedColumns = columns,
        hasRealtimeEnabled = true,
        isSchemaInspected = true
      )
      schemaInfo = info
      Log.d(TAG, "Inspected schema: products='$detectedProducts', categories='$detectedCategories', cols=$columns")
      Result.success(info)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to inspect database schema", e)
      val fallback = SupabaseSchemaInfo(
        detectedProductsTable = "products",
        detectedCategoriesTable = "categories",
        isSchemaInspected = true
      )
      schemaInfo = fallback
      Result.success(fallback)
    }
  }

  /**
   * Fetches all products from the Supabase products table.
   */
  suspend fun fetchProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
    if (!config.isConfigured()) {
      return@withContext Result.failure(IllegalStateException("Supabase credentials not configured. Please set SUPABASE_URL and SUPABASE_ANON_KEY."))
    }

    val url = config.getSupabaseUrl()
    val anonKey = config.getAnonKey()
    val tableName = schemaInfo.detectedProductsTable.ifBlank { "products" }

    try {
      // First attempt with order by created_at.desc
      var requestUrl = "$url/rest/v1/$tableName?select=*"
      var request = Request.Builder()
        .url(requestUrl)
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .addHeader("Accept", "application/json")
        .build()

      var response = httpClient.newCall(request).execute()

      if (!response.isSuccessful) {
        val errorBody = response.body?.string().orEmpty()
        Log.e(TAG, "Fetch products failed: HTTP ${response.code} $errorBody")
        return@withContext Result.failure(Exception("Supabase HTTP ${response.code}: $errorBody"))
      }

      val bodyStr = response.body?.string().orEmpty()
      val jsonArray = JSONArray(bodyStr)
      val productsList = mutableListOf<Product>()

      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val product = parseProductFromJson(obj)
        // Requirements: only active/published products should appear as available
        if (product != null) {
          productsList.add(product)
        }
      }

      Log.d(TAG, "Successfully fetched ${productsList.size} products from Supabase ($tableName)")
      Result.success(productsList)
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching products from Supabase", e)
      Result.failure(e)
    }
  }

  /**
   * Fetches all categories from the Supabase categories table.
   */
  suspend fun fetchCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
    if (!config.isConfigured()) {
      return@withContext Result.failure(IllegalStateException("Supabase not configured."))
    }

    val url = config.getSupabaseUrl()
    val anonKey = config.getAnonKey()
    val tableName = schemaInfo.detectedCategoriesTable.ifBlank { "categories" }

    try {
      val request = Request.Builder()
        .url("$url/rest/v1/$tableName?select=*")
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .addHeader("Accept", "application/json")
        .build()

      val response = httpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        val err = response.body?.string().orEmpty()
        return@withContext Result.failure(Exception("Supabase categories error: HTTP ${response.code} $err"))
      }

      val bodyStr = response.body?.string().orEmpty()
      val jsonArray = JSONArray(bodyStr)
      val categoryList = mutableListOf<Category>()

      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val cat = parseCategoryFromJson(obj)
        if (cat != null) {
          categoryList.add(cat)
        }
      }

      Log.d(TAG, "Fetched ${categoryList.size} categories from Supabase")
      Result.success(categoryList)
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching categories from Supabase", e)
      Result.failure(e)
    }
  }

  /**
   * Persists an order to the Supabase orders table (and order_items table).
   */
  suspend fun saveOrder(order: CustomerOrder): Result<String> = withContext(Dispatchers.IO) {
    if (!config.isConfigured()) {
      return@withContext Result.failure(IllegalStateException("Supabase is not configured"))
    }
    val url = config.getSupabaseUrl()
    val anonKey = config.getAnonKey()

    try {
      val payload = JSONObject().apply {
        put("order_number", order.orderNumber)
        put("customer_name", order.customerName)
        put("customer_email", order.email.ifBlank { "guest@arshadcollection.pk" })
        put("customer_phone", order.phone)
        put("shipping_address", order.shippingAddress)
        put("city", order.city)
        put("notes", order.notes.ifBlank { order.deliveryInstructions })
        put("total", order.total)
        put("subtotal", order.subtotal)
        put("status", order.orderStatus.name.lowercase(Locale.ROOT))
      }

      val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
      val request = Request.Builder()
        .url("$url/rest/v1/orders")
        .post(requestBody)
        .addHeader("Content-Type", "application/json")
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .addHeader("Prefer", "return=representation")
        .build()

      val response = httpClient.newCall(request).execute()
      val bodyStr = response.body?.string().orEmpty()

      if (response.isSuccessful) {
        Log.d(TAG, "Order #${order.orderNumber} successfully saved to Supabase: $bodyStr")
        try {
          val jsonArr = JSONArray(bodyStr)
          if (jsonArr.length() > 0) {
            val insertedOrderObj = jsonArr.getJSONObject(0)
            val orderId = insertedOrderObj.opt("id")
            if (orderId != null) {
              val itemsArr = JSONArray()
              for (item in order.items) {
                itemsArr.put(JSONObject().apply {
                  put("order_id", orderId)
                  put("product_id", item.productId)
                  put("product_name", item.productName)
                  put("unit_price", item.unitPrice)
                  put("quantity", item.quantity)
                })
              }
              val itemsReq = Request.Builder()
                .url("$url/rest/v1/order_items")
                .post(itemsArr.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .build()
              httpClient.newCall(itemsReq).execute()
            }
          }
        } catch (e: Exception) {
          Log.w(TAG, "Order items sync notice: ${e.message}")
        }
        Result.success(order.orderNumber)
      } else {
        Log.w(TAG, "Supabase saveOrder returned HTTP ${response.code}: $bodyStr")
        Result.failure(Exception("Supabase HTTP ${response.code}: $bodyStr"))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Exception saving order to Supabase", e)
      Result.failure(e)
    }
  }

  /**
   * Uploads raw binary image bytes to Supabase Storage bucket.
   * If successful, returns the public HTTPS URL for the uploaded object.
   */
  suspend fun uploadStorageFile(
    bucket: String,
    path: String,
    bytes: ByteArray,
    mimeType: String = "image/jpeg"
  ): Result<String> = withContext(Dispatchers.IO) {
    if (!config.isConfigured()) {
      return@withContext Result.failure(IllegalStateException("Supabase is not configured"))
    }
    val baseUrl = config.getSupabaseUrl()
    val anonKey = config.getAnonKey()
    val cleanPath = path.trimStart('/')
    val uploadUrl = "$baseUrl/storage/v1/object/$bucket/$cleanPath"

    try {
      val mediaType = mimeType.toMediaType()
      val requestBody = bytes.toRequestBody(mediaType)
      val request = Request.Builder()
        .url(uploadUrl)
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .addHeader("x-upsert", "true")
        .post(requestBody)
        .build()

      val response = httpClient.newCall(request).execute()
      if (response.isSuccessful) {
        val publicUrl = "$baseUrl/storage/v1/object/public/$bucket/$cleanPath"
        Result.success(publicUrl)
      } else {
        val err = response.body?.string().orEmpty()
        Log.e(TAG, "Storage upload failed: HTTP ${response.code} $err")
        Result.failure(Exception("Supabase storage error HTTP ${response.code}: $err"))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Storage upload exception", e)
      Result.failure(e)
    }
  }

  /**
   * Deletes an object from Supabase Storage bucket.
   */
  suspend fun deleteStorageFile(
    bucket: String,
    path: String
  ): Result<Unit> = withContext(Dispatchers.IO) {
    if (!config.isConfigured()) {
      return@withContext Result.success(Unit)
    }
    val baseUrl = config.getSupabaseUrl()
    val anonKey = config.getAnonKey()
    val cleanPath = path.trimStart('/')
    val deleteUrl = "$baseUrl/storage/v1/object/$bucket/$cleanPath"

    try {
      val request = Request.Builder()
        .url(deleteUrl)
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .delete()
        .build()

      val response = httpClient.newCall(request).execute()
      if (response.isSuccessful) {
        Result.success(Unit)
      } else {
        Result.failure(Exception("Supabase storage delete HTTP ${response.code}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Parses a single Product from a Supabase row object.
   * Tolerant to varying column naming conventions across Supabase setups.
   */
  fun parseProductFromJson(obj: JSONObject): Product? {
    try {
      val rawId = when {
        obj.has("id") -> obj.opt("id")?.toString() ?: ""
        obj.has("product_id") -> obj.opt("product_id")?.toString() ?: ""
        else -> ""
      }
      if (rawId.isBlank()) return null

      val numericId = rawId.toLongOrNull() ?: (kotlin.math.abs(rawId.hashCode().toLong()).let { if (it == 0L) 1L else it })
      val name = obj.optString("name").ifBlank { obj.optString("title").ifBlank { "Arshad Product" } }
      val slug = obj.optString("slug").ifBlank { name.lowercase().replace("[^a-z0-9]+".toRegex(), "-") }
      val description = obj.optString("description").ifBlank { obj.optString("details") }
      val shortDescription = obj.optString("short_description").ifBlank { obj.optString("summary") }
      val categoryId = obj.optString("category_id").ifBlank { obj.optString("category").ifBlank { "men" } }
      val subcategoryId = obj.optString("subcategory_id").ifBlank { obj.optString("subcategory") }
      val brand = obj.optString("brand").ifBlank { "Arshad Collection" }
      val sku = obj.optString("sku").ifBlank { "AC-$rawId" }

      val price = when {
        obj.has("price") -> obj.optDouble("price", 0.0)
        obj.has("regular_price") -> obj.optDouble("regular_price", 0.0)
        obj.has("amount") -> obj.optDouble("amount", 0.0)
        else -> 0.0
      }

      val salePrice: Double? = when {
        obj.has("sale_price") && !obj.isNull("sale_price") -> obj.optDouble("sale_price")
        obj.has("discount_price") && !obj.isNull("discount_price") -> obj.optDouble("discount_price")
        obj.has("compare_at_price") && !obj.isNull("compare_at_price") -> obj.optDouble("compare_at_price")
        else -> null
      }

      val costPrice: Double? = if (obj.has("cost_price") && !obj.isNull("cost_price")) obj.optDouble("cost_price") else null

      val stock = when {
        obj.has("stock") -> obj.optInt("stock", 10)
        obj.has("inventory_quantity") -> obj.optInt("inventory_quantity", 10)
        obj.has("quantity") -> obj.optInt("quantity", 10)
        obj.has("stock_quantity") -> obj.optInt("stock_quantity", 10)
        else -> 10
      }

      val lowStockThreshold = obj.optInt("low_stock_threshold", 3)

      // Publication/Active status check:
      // When a product is unpublished, isActive becomes false.
      val isActive: Boolean = when {
        obj.has("is_active") -> obj.optBoolean("is_active", true)
        obj.has("is_published") -> obj.optBoolean("is_published", true)
        obj.has("published") -> obj.optBoolean("published", true)
        obj.has("status") -> {
          val st = obj.optString("status")
          st.equals("published", ignoreCase = true) || st.equals("active", ignoreCase = true)
        }
        else -> true
      }

      // Images parsing:
      val imagesList = mutableListOf<String>()
      if (obj.has("images")) {
        val rawImages = obj.opt("images")
        if (rawImages is JSONArray) {
          for (j in 0 until rawImages.length()) {
            val img = rawImages.optString(j)
            if (img.isNotBlank()) imagesList.add(config.resolveImageUrl(img))
          }
        } else if (rawImages is String && rawImages.isNotBlank()) {
          try {
            val arr = JSONArray(rawImages)
            for (j in 0 until arr.length()) {
              imagesList.add(config.resolveImageUrl(arr.getString(j)))
            }
          } catch (e: Exception) {
            imagesList.add(config.resolveImageUrl(rawImages))
          }
        }
      }
      if (imagesList.isEmpty()) {
        val singleImg = obj.optString("image_url").ifBlank { obj.optString("featured_image").ifBlank { obj.optString("image") } }
        if (singleImg.isNotBlank()) {
          imagesList.add(config.resolveImageUrl(singleImg))
        }
      }

      // Sizes parsing
      val sizesList = mutableListOf<String>()
      if (obj.has("sizes")) {
        val rawSizes = obj.opt("sizes")
        if (rawSizes is JSONArray) {
          for (j in 0 until rawSizes.length()) sizesList.add(rawSizes.optString(j))
        } else if (rawSizes is String && rawSizes.isNotBlank()) {
          rawSizes.split(",", ";").forEach { if (it.isNotBlank()) sizesList.add(it.trim()) }
        }
      }

      // Colors parsing
      val colorsList = mutableListOf<String>()
      if (obj.has("colors")) {
        val rawColors = obj.opt("colors")
        if (rawColors is JSONArray) {
          for (j in 0 until rawColors.length()) colorsList.add(rawColors.optString(j))
        } else if (rawColors is String && rawColors.isNotBlank()) {
          rawColors.split(",", ";").forEach { if (it.isNotBlank()) colorsList.add(it.trim()) }
        }
      }

      // Tags parsing
      val tagsList = mutableListOf<String>()
      if (obj.has("tags")) {
        val rawTags = obj.opt("tags")
        if (rawTags is JSONArray) {
          for (j in 0 until rawTags.length()) tagsList.add(rawTags.optString(j))
        } else if (rawTags is String && rawTags.isNotBlank()) {
          rawTags.split(",").forEach { if (it.isNotBlank()) tagsList.add(it.trim()) }
        }
      }

      val rating = obj.optDouble("rating", 4.8).toFloat()
      val reviewCount = obj.optInt("review_count", 12)
      val isFeatured = obj.optBoolean("is_featured", false)
      val isBestSeller = obj.optBoolean("is_best_seller", false)
      val isNewArrival = obj.optBoolean("is_new_arrival", false)

      var createdAt = System.currentTimeMillis()
      if (obj.has("created_at")) {
        val dateStr = obj.optString("created_at")
        if (dateStr.isNotBlank()) {
          try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val d = sdf.parse(dateStr)
            if (d != null) createdAt = d.time
          } catch (e: Exception) {
            // keep current time
          }
        }
      }

      return Product(
        id = numericId,
        supabaseId = rawId,
        name = name,
        slug = slug,
        description = description,
        shortDescription = shortDescription,
        categoryId = categoryId,
        subcategoryId = subcategoryId,
        brand = brand,
        sku = sku,
        images = imagesList,
        price = price,
        salePrice = salePrice,
        costPrice = costPrice,
        stock = stock,
        lowStockThreshold = lowStockThreshold,
        sizes = sizesList,
        colors = colorsList,
        tags = tagsList,
        rating = rating,
        reviewCount = reviewCount,
        isFeatured = isFeatured,
        isBestSeller = isBestSeller,
        isNewArrival = isNewArrival,
        isActive = isActive,
        createdAt = createdAt
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing product from JSON", e)
      return null
    }
  }

  /**
   * Parses a single Category from a Supabase row object.
   */
  fun parseCategoryFromJson(obj: JSONObject): Category? {
    try {
      val rawId = obj.optString("id").ifBlank { obj.optString("slug") }
      if (rawId.isBlank()) return null
      val name = obj.optString("name").ifBlank { obj.optString("title").ifBlank { "Category" } }
      val slug = obj.optString("slug").ifBlank { name.lowercase().replace("[^a-z0-9]+".toRegex(), "-") }
      val imageResName = obj.optString("image_url").ifBlank {
        obj.optString("image_res_name").ifBlank { obj.optString("image") }
      }
      val productCount = obj.optInt("product_count", 0)
      val sortOrder = obj.optInt("sort_order", 0)
      val isActive = obj.optBoolean("is_active", true)

      val subcategoriesList = mutableListOf<String>()
      if (obj.has("subcategories")) {
        val rawSubs = obj.opt("subcategories")
        if (rawSubs is JSONArray) {
          for (j in 0 until rawSubs.length()) subcategoriesList.add(rawSubs.optString(j))
        } else if (rawSubs is String && rawSubs.isNotBlank()) {
          rawSubs.split(",").forEach { if (it.isNotBlank()) subcategoriesList.add(it.trim()) }
        }
      }

      return Category(
        id = rawId,
        name = name,
        slug = slug,
        imageResName = imageResName,
        productCount = productCount,
        subcategories = subcategoriesList,
        sortOrder = sortOrder,
        isActive = isActive
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing category from JSON", e)
      return null
    }
  }
}
