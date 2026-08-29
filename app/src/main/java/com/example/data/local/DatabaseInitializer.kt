package com.example.data.local

import org.json.JSONArray

object DatabaseInitializer {

  private fun listToJson(list: List<String>): String {
    val arr = JSONArray()
    list.forEach { arr.put(it) }
    return arr.toString()
  }

  suspend fun seedDatabase(database: ArshadDatabase) {
    // 1. Seed Categories with subcategories
    val categories = listOf(
      CategoryEntity(
        id = "men",
        name = "Men's Wear",
        slug = "men",
        imageResName = "cat_men_wear_1787981550518",
        productCount = 6,
        subcategoriesJson = listToJson(listOf("Shalwar Kameez", "Designer Kurta", "Waistcoats", "Formal Shirts", "Pants", "Accessories")),
        sortOrder = 1
      ),
      CategoryEntity(
        id = "women",
        name = "Women's Wear",
        slug = "women",
        imageResName = "cat_women_wear_1787981564444",
        productCount = 5,
        subcategoriesJson = listToJson(listOf("Embroidered Suits", "Abayas", "Dupattas", "Pret Wear", "Accessories")),
        sortOrder = 2
      ),
      CategoryEntity(
        id = "home_textile",
        name = "Home Textile",
        slug = "home-textile",
        imageResName = "cat_home_textile_1787981585422",
        productCount = 4,
        subcategoriesJson = listToJson(listOf("Bedsheets", "Pillow Covers", "Luxury Curtains", "Quilt Covers")),
        sortOrder = 3
      ),
      CategoryEntity(
        id = "crockery",
        name = "Crockery & Dining",
        slug = "crockery",
        imageResName = "cat_crockery_set_1787981622532",
        productCount = 3,
        subcategoriesJson = listToJson(listOf("Dinner Sets", "Tea Sets", "Cutlery", "Serving Bowls")),
        sortOrder = 4
      ),
      CategoryEntity(
        id = "cultural",
        name = "Handmade Cultural",
        slug = "cultural",
        imageResName = "cat_cultural_decor_1787981608475",
        productCount = 3,
        subcategoriesJson = listToJson(listOf("Multani Pottery", "Brass Handicrafts", "Ajrak & Shawls", "Wooden Inlay")),
        sortOrder = 5
      ),
      CategoryEntity(
        id = "gadgets",
        name = "Smart Gadgets",
        slug = "gadgets",
        imageResName = "cat_gadgets_watch_1787981639819",
        productCount = 3,
        subcategoriesJson = listToJson(listOf("Smart Watches", "Earbuds", "Power Banks", "Lifestyle Tech")),
        sortOrder = 6
      ),
      CategoryEntity(
        id = "kids",
        name = "Kids' Collection",
        slug = "kids",
        imageResName = "cat_men_wear_1787981550518",
        productCount = 2,
        subcategoriesJson = listToJson(listOf("Boys Kurta", "Girls Festive", "Baby Clothing")),
        sortOrder = 7
      ),
      CategoryEntity(
        id = "beauty",
        name = "Beauty & Care",
        slug = "beauty",
        imageResName = "cat_women_wear_1787981564444",
        productCount = 2,
        subcategoriesJson = listToJson(listOf("Organic Serums", "Fragrances", "Hair Care")),
        sortOrder = 8
      ),
      CategoryEntity(
        id = "sports",
        name = "Sports & Fitness",
        slug = "sports",
        imageResName = "cat_gadgets_watch_1787981639819",
        productCount = 2,
        subcategoriesJson = listToJson(listOf("Tracksuits", "Gym Wear", "Accessories")),
        sortOrder = 9
      )
    )
    database.categoryDao().insertAll(categories)

    // 2. Seed Rich Product Catalog
    val products = listOf(
      ProductEntity(
        name = "Royal Gold-Embroidered Men's Shalwar Kameez",
        slug = "royal-gold-embroidered-mens-shalwar-kameez",
        description = "Crafted with finest Egyptian blended cotton featuring exquisite gold thread needlework on collar, cuffs, and placket. Impeccable tailoring and breathability for festive occasions, Eid, and formal gatherings.",
        shortDescription = "Premium Egyptian blended cotton with signature gold embroidery detailing.",
        categoryId = "men",
        subcategoryId = "Shalwar Kameez",
        brand = "Arshad Collection",
        sku = "AC-MEN-001",
        imagesJson = listToJson(listOf("cat_men_wear_1787981550518", "hero_banner_luxury_1787981532790")),
        price = 4999.0,
        salePrice = 3999.0,
        costPrice = 2400.0,
        stock = 15,
        lowStockThreshold = 4,
        sizesJson = listToJson(listOf("S", "M", "L", "XL", "XXL")),
        colorsJson = listToJson(listOf("Jet Black", "Royal Navy", "Ivory White", "Charcoal")),
        tagsJson = listToJson(listOf("Eid", "Festive", "Shalwar Kameez", "Luxury")),
        rating = 4.9f,
        reviewCount = 38,
        isFeatured = true,
        isBestSeller = true,
        isNewArrival = true
      ),
      ProductEntity(
        name = "Handcrafted Velvet Kurta & Waistcoat Set",
        slug = "handcrafted-velvet-kurta-waistcoat-set",
        description = "Deep obsidian micro-velvet waistcoat with intricate antique gold zari motif work, paired with premium soft cotton silk kurta trousers.",
        shortDescription = "Micro-velvet waistcoat with zari motifs and silk kurta trousers.",
        categoryId = "men",
        subcategoryId = "Waistcoats",
        brand = "Arshad Collection",
        sku = "AC-MEN-002",
        imagesJson = listToJson(listOf("cat_men_wear_1787981550518")),
        price = 6499.0,
        salePrice = 5299.0,
        costPrice = 3100.0,
        stock = 8,
        lowStockThreshold = 3,
        sizesJson = listToJson(listOf("M", "L", "XL")),
        colorsJson = listToJson(listOf("Obsidian Black", "Maroon Crimson", "Emerald Green")),
        tagsJson = listToJson(listOf("Wedding", "Waistcoat", "Velvet")),
        rating = 4.8f,
        reviewCount = 22,
        isFeatured = true,
        isBestSeller = false,
        isNewArrival = true
      ),
      ProductEntity(
        name = "Luxury Chiffon Embroidered 3-Piece Festive Suit",
        slug = "luxury-chiffon-embroidered-3-piece-festive-suit",
        description = "Heavily embellished chiffon shirt with sequins, tilla work, and pearls. Includes embroidered pure organza dupatta with four-sided borders and raw silk trousers.",
        shortDescription = "Designer 3-piece chiffon suit with tilla embroidery and organza dupatta.",
        categoryId = "women",
        subcategoryId = "Embroidered Suits",
        brand = "Arshad Collection",
        sku = "AC-WMN-001",
        imagesJson = listToJson(listOf("cat_women_wear_1787981564444", "hero_banner_luxury_1787981532790")),
        price = 8999.0,
        salePrice = 7499.0,
        costPrice = 4500.0,
        stock = 12,
        lowStockThreshold = 3,
        sizesJson = listToJson(listOf("XS", "S", "M", "L", "Unstitched")),
        colorsJson = listToJson(listOf("Champagne Gold", "Blush Pink", "Midnight Black")),
        tagsJson = listToJson(listOf("Women", "Festive", "3-Piece", "Embroidered")),
        rating = 5.0f,
        reviewCount = 45,
        isFeatured = true,
        isBestSeller = true,
        isNewArrival = true
      ),
      ProductEntity(
        name = "Premium Silk Embroidered Bridal & Luxury Abaya",
        slug = "premium-silk-embroidered-bridal-luxury-abaya",
        description = "Imported Nida matte silk abaya with royal gold calligraphy embroidery down the front and sleeves. Breathable, fluid drape with matching hijab.",
        shortDescription = "Matte silk abaya with gold sleeve calligraphy and matching hijab.",
        categoryId = "women",
        subcategoryId = "Abayas",
        brand = "Arshad Collection",
        sku = "AC-WMN-002",
        imagesJson = listToJson(listOf("cat_women_wear_1787981564444")),
        price = 5499.0,
        salePrice = 4499.0,
        costPrice = 2800.0,
        stock = 10,
        lowStockThreshold = 2,
        sizesJson = listToJson(listOf("52", "54", "56", "58")),
        colorsJson = listToJson(listOf("Deep Black", "Coffee Brown", "Olive")),
        tagsJson = listToJson(listOf("Abaya", "Modest", "Silk")),
        rating = 4.8f,
        reviewCount = 19,
        isFeatured = false,
        isBestSeller = true,
        isNewArrival = false
      ),
      ProductEntity(
        name = "King Size Luxury Satin Sateen Bedsheet Set (6 Pcs)",
        slug = "king-size-luxury-satin-sateen-bedsheet-set",
        description = "300-thread-count high-density Egyptian cotton sateen bedsheet set. Includes 1 King Fitted sheet, 1 Flat sheet, 2 Embroidered pillow covers, and 2 Cushion cases. Ultra-soft cooling texture.",
        shortDescription = "300TC Egyptian cotton sateen king bedsheet set with 4 pillow/cushion covers.",
        categoryId = "home_textile",
        subcategoryId = "Bedsheets",
        brand = "Arshad Home",
        sku = "AC-HTX-001",
        imagesJson = listToJson(listOf("cat_home_textile_1787981585422")),
        price = 4500.0,
        salePrice = 3499.0,
        costPrice = 2100.0,
        stock = 20,
        lowStockThreshold = 5,
        sizesJson = listToJson(listOf("King (95x100 in)", "Queen (90x95 in)")),
        colorsJson = listToJson(listOf("Gold & Charcoal", "Royal Navy", "Pearl White", "Dusty Rose")),
        tagsJson = listToJson(listOf("Bedsheet", "Home Textile", "King Size", "Cotton")),
        rating = 4.9f,
        reviewCount = 52,
        isFeatured = true,
        isBestSeller = true,
        isNewArrival = true
      ),
      ProductEntity(
        name = "Velvet Jacquard Blackout Luxury Curtains (Pair)",
        slug = "velvet-jacquard-blackout-luxury-curtains",
        description = "Heavyweight thermal blackout jacquard velvet curtains with metallic gold damask weaving. 85% light blocking, rust-proof metal eyelets.",
        shortDescription = "Blackout velvet jacquard curtains pair with gold damask motifs.",
        categoryId = "home_textile",
        subcategoryId = "Luxury Curtains",
        brand = "Arshad Home",
        sku = "AC-HTX-002",
        imagesJson = listToJson(listOf("cat_home_textile_1787981585422")),
        price = 5999.0,
        salePrice = 4899.0,
        costPrice = 3200.0,
        stock = 6,
        lowStockThreshold = 2,
        sizesJson = listToJson(listOf("7 ft (Standard)", "8.5 ft (Long)")),
        colorsJson = listToJson(listOf("Gold & Black", "Rich Bronze", "Silver Grey")),
        tagsJson = listToJson(listOf("Curtains", "Blackout", "Home Decor")),
        rating = 4.7f,
        reviewCount = 14,
        isFeatured = false,
        isBestSeller = false,
        isNewArrival = true
      ),
      ProductEntity(
        name = "Royal Gold-Rimmed Porcelain Dinner Set (36 Pcs)",
        slug = "royal-gold-rimmed-porcelain-dinner-set-36-pcs",
        description = "Fine bone china ceramic 36-piece complete dining set. Features 24k gold leaf rim detailing, microwave-safe non-scratch glaze, 8 large plates, 8 quarter plates, 8 dessert bowls, 2 serving platters, and 2 curry bowls with lids.",
        shortDescription = "24k gold leaf trimmed bone china 36-piece formal dinner set.",
        categoryId = "crockery",
        subcategoryId = "Dinner Sets",
        brand = "Arshad Living",
        sku = "AC-CRK-001",
        imagesJson = listToJson(listOf("cat_crockery_set_1787981622532")),
        price = 14999.0,
        salePrice = 12499.0,
        costPrice = 8500.0,
        stock = 5,
        lowStockThreshold = 2,
        sizesJson = listToJson(listOf("36 Pcs Set", "72 Pcs Full Luxury")),
        colorsJson = listToJson(listOf("Ivory Gold", "Black Marble Gold")),
        tagsJson = listToJson(listOf("Crockery", "Dinner Set", "Luxury Tableware")),
        rating = 5.0f,
        reviewCount = 18,
        isFeatured = true,
        isBestSeller = true,
        isNewArrival = false
      ),
      ProductEntity(
        name = "Handmade Multani Blue Pottery Artisanal Vase",
        slug = "handmade-multani-blue-pottery-artisanal-vase",
        description = "Master artisan crafted traditional glazed clay Multani pottery vase. Hand-painted floral kashikari motifs with natural cobalt oxides. Iconic Pakistani cultural heritage piece.",
        shortDescription = "Authentic hand-painted Multani Kashikari glazed pottery vase.",
        categoryId = "cultural",
        subcategoryId = "Multani Pottery",
        brand = "Arshad Heritage",
        sku = "AC-CLT-001",
        imagesJson = listToJson(listOf("cat_cultural_decor_1787981608475")),
        price = 3200.0,
        salePrice = 2499.0,
        costPrice = 1400.0,
        stock = 14,
        lowStockThreshold = 4,
        sizesJson = listToJson(listOf("12 Inches", "16 Inches")),
        colorsJson = listToJson(listOf("Cobalt Blue", "Turquoise")),
        tagsJson = listToJson(listOf("Cultural", "Multani Pottery", "Handmade", "Decor")),
        rating = 4.9f,
        reviewCount = 29,
        isFeatured = true,
        isBestSeller = true,
        isNewArrival = true
      ),
      ProductEntity(
        name = "Hand-Carved Brass Antique Samovar & Tea Set",
        slug = "hand-carved-brass-antique-samovar-tea-set",
        description = "Traditional vintage brass samovar with detailed floral filigree chasing. Complete with 6 brass cups and engraved presentation tray.",
        shortDescription = "Antique brass samovar with intricate filigree engraving and 6 cups.",
        categoryId = "cultural",
        subcategoryId = "Brass Handicrafts",
        brand = "Arshad Heritage",
        sku = "AC-CLT-002",
        imagesJson = listToJson(listOf("cat_cultural_decor_1787981608475")),
        price = 7800.0,
        salePrice = 6499.0,
        costPrice = 4200.0,
        stock = 4,
        lowStockThreshold = 2,
        sizesJson = listToJson(listOf("Standard")),
        colorsJson = listToJson(listOf("Antique Brass Gold")),
        tagsJson = listToJson(listOf("Brass", "Samovar", "Handicrafts")),
        rating = 4.8f,
        reviewCount = 11,
        isFeatured = false,
        isBestSeller = false,
        isNewArrival = true
      ),
      ProductEntity(
        name = "Arshad Luxury Edition AMOLED Smart Watch",
        slug = "arshad-luxury-edition-amoled-smart-watch",
        description = "High-definition 1.43\" AMOLED display, Bluetooth calling with HD mic, 24/7 heart rate & SpO2 tracker, IP68 water resistance, and premium stainless steel gold magnetic strap + silicone sports strap included.",
        shortDescription = "1.43\" AMOLED Bluetooth calling smartwatch with luxury gold stainless steel strap.",
        categoryId = "gadgets",
        subcategoryId = "Smart Watches",
        brand = "Arshad Tech",
        sku = "AC-GDT-001",
        imagesJson = listToJson(listOf("cat_gadgets_watch_1787981639819")),
        price = 7499.0,
        salePrice = 5999.0,
        costPrice = 3800.0,
        stock = 18,
        lowStockThreshold = 4,
        sizesJson = listToJson(listOf("46mm")),
        colorsJson = listToJson(listOf("Gold Stainless Steel", "Matte Black Titanium")),
        tagsJson = listToJson(listOf("Gadget", "Smart Watch", "AMOLED")),
        rating = 4.9f,
        reviewCount = 67,
        isFeatured = true,
        isBestSeller = true,
        isNewArrival = true
      ),
      ProductEntity(
        name = "Active Noise Cancelling Wireless Pro Earbuds",
        slug = "active-noise-cancelling-wireless-pro-earbuds",
        description = "Hybrid 40dB Active Noise Cancellation, dual microphones for crystal-clear calls, 36-hour battery backup with fast wireless charging case.",
        shortDescription = "40dB Hybrid ANC wireless earbuds with 36hr battery backup.",
        categoryId = "gadgets",
        subcategoryId = "Earbuds",
        brand = "Arshad Tech",
        sku = "AC-GDT-002",
        imagesJson = listToJson(listOf("cat_gadgets_watch_1787981639819")),
        price = 4499.0,
        salePrice = 3299.0,
        costPrice = 1900.0,
        stock = 25,
        lowStockThreshold = 5,
        sizesJson = listToJson(listOf("One Size")),
        colorsJson = listToJson(listOf("Obsidian Black", "Gold Accented White")),
        tagsJson = listToJson(listOf("Earbuds", "Wireless", "Audio")),
        rating = 4.7f,
        reviewCount = 31,
        isFeatured = false,
        isBestSeller = true,
        isNewArrival = false
      )
    )
    database.productDao().insertAll(products)

    // 3. Seed Active Promotional Coupons
    val coupons = listOf(
      CouponEntity(
        code = "ARSHAD10",
        discountType = "PERCENTAGE",
        discountValue = 10.0,
        minOrderAmount = 2000.0,
        maxDiscount = 1000.0,
        expiryDate = "31 Dec 2026",
        usageLimit = 500,
        isActive = true
      ),
      CouponEntity(
        code = "WELCOME500",
        discountType = "FIXED",
        discountValue = 500.0,
        minOrderAmount = 3000.0,
        maxDiscount = 500.0,
        expiryDate = "31 Dec 2026",
        usageLimit = 200,
        isActive = true
      ),
      CouponEntity(
        code = "EIDLUXURY",
        discountType = "PERCENTAGE",
        discountValue = 15.0,
        minOrderAmount = 5000.0,
        maxDiscount = 2000.0,
        expiryDate = "31 Dec 2026",
        usageLimit = 100,
        isActive = true
      )
    )
    database.couponDao().insertAll(coupons)

    // 4. Seed Homepage Banners
    val banners = listOf(
      BannerEntity(
        title = "Festive Luxury Collection",
        subtitle = "Style • Quality • Trust | Up to 25% Off Across Pakistan",
        buttonText = "Shop Collection",
        targetCategory = "men",
        imageResName = "hero_banner_luxury_1787981532790",
        isActive = true
      ),
      BannerEntity(
        title = "Royal Home Textiles",
        subtitle = "Egyptian Cotton Bedsheets & Velvet Jacquard Curtains",
        buttonText = "Explore Home",
        targetCategory = "home_textile",
        imageResName = "cat_home_textile_1787981585422",
        isActive = true
      ),
      BannerEntity(
        title = "Multani Artisanal Handicrafts",
        subtitle = "Authentic Blue Pottery & Brass Samovar Collections",
        buttonText = "View Cultural",
        targetCategory = "cultural",
        imageResName = "cat_cultural_decor_1787981608475",
        isActive = true
      )
    )
    database.bannerDao().insertAll(banners)

    // 5. Seed App Settings
    database.settingsDao().saveSettings(
      AppSettingsEntity(
        whatsappNumber = "03413399629",
        supportEmail = "info.arshadcollection@gmail.com",
        currencySymbol = "Rs.",
        defaultDeliveryFee = 200.0,
        freeDeliveryThreshold = 3000.0,
        isMaintenanceMode = false,
        aboutText = "Arshad Collection is an online retail store focused on bringing customers quality products across fashion, home textiles, lifestyle, beauty, gadgets, cultural products and more.",
        tagline = "Style • Quality • Trust",
        ownerName = "Arshad Ahmed"
      )
    )

    // 6. Seed Sample Initial Reviews
    val reviews = listOf(
      ReviewEntity(
        productId = 1,
        customerName = "Muhammad Bilal (Lahore)",
        rating = 5.0f,
        reviewText = "MashaAllah the Shalwar Kameez quality is outstanding! The gold embroidery on the collar looks very graceful. Delivered in 2 days to Lahore via COD.",
        isVerifiedPurchase = true,
        createdAt = System.currentTimeMillis() - 86400000L * 3,
        isApproved = true
      ),
      ReviewEntity(
        productId = 1,
        customerName = "Hamza Farooq (Islamabad)",
        rating = 5.0f,
        reviewText = "Fabric is super breathable and premium. Fit was perfect as per chart. Arshad Collection is my new favorite brand.",
        isVerifiedPurchase = true,
        createdAt = System.currentTimeMillis() - 86400000L * 7,
        isApproved = true
      ),
      ReviewEntity(
        productId = 3,
        customerName = "Ayesha Siddiqua (Karachi)",
        rating = 5.0f,
        reviewText = "The chiffon dress exceeded my expectations! Exactly as shown in photos with genuine tilla work. Received lots of compliments!",
        isVerifiedPurchase = true,
        createdAt = System.currentTimeMillis() - 86400000L * 2,
        isApproved = true
      ),
      ReviewEntity(
        productId = 5,
        customerName = "Mrs. Tariq (Faisalabad)",
        rating = 5.0f,
        reviewText = "The satin sateen bedsheet is so silky and luxurious! Will definitely order more for guest rooms.",
        isVerifiedPurchase = true,
        createdAt = System.currentTimeMillis() - 86400000L * 5,
        isApproved = true
      )
    )
    reviews.forEach { database.reviewDao().insertReview(it) }

    // 7. Seed Initial Notifications
    database.notificationDao().insertNotification(
      NotificationEntity(
        title = "Welcome to Arshad Collection!",
        message = "Enjoy free delivery on all orders above Rs. 3,000 across Pakistan with code ARSHAD10.",
        type = "PROMO"
      )
    )

    // 8. Seed Official Admin Account (Securely hashed with unique cryptographic salt)
    val adminSalt = "ac_admin_sec_salt_987654"
    val adminPasswordHash = com.example.util.SecurityHelper.hashPassword("Arshad@548", adminSalt)
    val primaryAdminEmail = "info.arshadcolletion@gmail.com"
    val altAdminEmail = "info.arshadcollection@gmail.com"

    if (!database.userDao().isEmailTaken(primaryAdminEmail)) {
      database.userDao().insertUser(
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
    }

    if (!database.userDao().isEmailTaken(altAdminEmail)) {
      database.userDao().insertUser(
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
    }
  }
}
