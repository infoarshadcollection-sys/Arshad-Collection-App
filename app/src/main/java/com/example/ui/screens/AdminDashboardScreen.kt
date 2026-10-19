package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import com.example.ui.components.ProductImageView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.ArshadBrandLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
  viewModel: ShopViewModel,
  onExitAdmin: () -> Unit
) {
  val isAdmin by viewModel.isAdminLoggedIn.collectAsState()
  val adminTab by viewModel.adminSelectedTab.collectAsState()
  val allProducts by viewModel.allProductsAdmin.collectAsState()
  val allOrders by viewModel.customerOrders.collectAsState()
  val allCategories by viewModel.categoriesAdmin.collectAsState()
  val allCoupons by viewModel.repository.allCouponsAdmin.collectAsState(emptyList())
  val appSettings by viewModel.appSettings.collectAsState()

  if (!isAdmin) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(BlackMain)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = RedDiscount,
          modifier = Modifier.size(64.dp)
        )
        Text(
          "Access Denied",
          color = Color.White,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          "This portal is restricted to authorized store administrators.",
          color = TextSecondaryDark,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
          onClick = onExitAdmin,
          colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Return to Store", fontWeight = FontWeight.Bold)
        }
      }
    }
    return
  }

  val totalRevenue = remember(allOrders) { allOrders.sumOf { it.total } }
  val pendingOrdersCount = remember(allOrders) { allOrders.count { it.orderStatus == OrderStatus.PENDING } }
  val deliveredOrdersCount = remember(allOrders) { allOrders.count { it.orderStatus == OrderStatus.DELIVERED } }
  val lowStockCount = remember(allProducts) { allProducts.count { it.isLowStock || !it.isInStock } }

  var showAddProductDialog by remember { mutableStateOf(false) }
  var productToEdit by remember { mutableStateOf<Product?>(null) }
  var showAddCategoryDialog by remember { mutableStateOf(false) }
  var categoryToEdit by remember { mutableStateOf<Category?>(null) }
  var showAddCouponDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            ArshadBrandLogo(size = 36)
            Column {
              Text("Admin Portal", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 16.sp)
              Text("Arshad Collection Management", color = TextSecondaryDark, fontSize = 11.sp)
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onExitAdmin) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Exit Admin", tint = GoldPrimary)
          }
        },
        actions = {
          IconButton(onClick = onExitAdmin) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Admin", tint = GoldPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackSurface)
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
    ) {
      // Admin Tabs Row
      ScrollableTabRow(
        selectedTabIndex = adminTab,
        containerColor = BlackSurface,
        contentColor = GoldPrimary,
        edgePadding = 16.dp
      ) {
        listOf("Overview", "Products", "Categories", "Orders", "Inventory", "Coupons", "Supabase Cloud", "Store Settings").forEachIndexed { index, title ->
          Tab(
            selected = adminTab == index,
            onClick = { viewModel.adminSelectedTab.value = index },
            text = { Text(title, fontWeight = if (adminTab == index) FontWeight.Bold else FontWeight.Normal) }
          )
        }
      }

      when (adminTab) {
        0 -> AdminOverviewView(
          totalRevenue = totalRevenue,
          ordersCount = allOrders.size,
          pendingCount = pendingOrdersCount,
          deliveredCount = deliveredOrdersCount,
          productsCount = allProducts.size,
          lowStockCount = lowStockCount,
          onTabSelect = { viewModel.adminSelectedTab.value = it }
        )
        1 -> AdminProductsView(
          products = allProducts,
          onAddProduct = { showAddProductDialog = true },
          onEditProduct = { productToEdit = it },
          onDeleteProduct = { viewModel.adminDeleteProduct(it) }
        )
        2 -> AdminCategoriesView(
          categories = allCategories,
          viewModel = viewModel,
          onAddCategory = { showAddCategoryDialog = true },
          onEditCategory = { categoryToEdit = it },
          onDeleteCategory = { viewModel.adminDeleteCategory(it) }
        )
        3 -> AdminOrdersView(
          orders = allOrders,
          onUpdateStatus = { id, st -> viewModel.adminUpdateOrderStatus(id, st) }
        )
        4 -> AdminInventoryView(
          products = allProducts,
          onUpdateStock = { id, stock -> viewModel.adminUpdateStock(id, stock) }
        )
        5 -> AdminCouponsView(
          coupons = allCoupons,
          onAddCoupon = { showAddCouponDialog = true },
          onDeleteCoupon = { viewModel.adminDeleteCoupon(it) }
        )
        6 -> AdminSupabaseView(
          viewModel = viewModel
        )
        7 -> AdminSettingsView(
          settings = appSettings,
          onSaveSettings = { viewModel.adminUpdateSettings(it) }
        )
      }
    }
  }

  // Add Product Dialog
  if (showAddProductDialog) {
    ProductFormDialog(
      initialProduct = null,
      categories = allCategories,
      viewModel = viewModel,
      onDismiss = { showAddProductDialog = false },
      onSave = { newProd ->
        viewModel.adminSaveProduct(newProd)
        showAddProductDialog = false
      }
    )
  }

  // Edit Product Dialog
  productToEdit?.let { prod ->
    ProductFormDialog(
      initialProduct = prod,
      categories = allCategories,
      viewModel = viewModel,
      onDismiss = { productToEdit = null },
      onSave = { updatedProd ->
        viewModel.adminSaveProduct(updatedProd)
        productToEdit = null
      }
    )
  }

  // Add Category Dialog
  if (showAddCategoryDialog) {
    CategoryFormDialog(
      initialCategory = null,
      viewModel = viewModel,
      onDismiss = { showAddCategoryDialog = false },
      onSave = { newCat ->
        viewModel.adminSaveCategory(newCat)
        showAddCategoryDialog = false
      }
    )
  }

  // Edit Category Dialog
  categoryToEdit?.let { cat ->
    CategoryFormDialog(
      initialCategory = cat,
      viewModel = viewModel,
      onDismiss = { categoryToEdit = null },
      onSave = { updatedCat ->
        viewModel.adminSaveCategory(updatedCat)
        categoryToEdit = null
      }
    )
  }

  // Add Coupon Dialog
  if (showAddCouponDialog) {
    AddCouponDialog(
      onDismiss = { showAddCouponDialog = false },
      onSave = { newCpn ->
        viewModel.adminSaveCoupon(newCpn)
        showAddCouponDialog = false
      }
    )
  }
}

@Composable
fun AdminOverviewView(
  totalRevenue: Double,
  ordersCount: Int,
  pendingCount: Int,
  deliveredCount: Int,
  productsCount: Int,
  lowStockCount: Int,
  onTabSelect: (Int) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text("Business Overview", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AdminStatCard(title = "Total Sales", value = CurrencyFormatter.format(totalRevenue), icon = Icons.Default.AttachMoney, color = GoldPrimary, modifier = Modifier.weight(1f))
        AdminStatCard(title = "Total Orders", value = "$ordersCount", icon = Icons.Default.Receipt, color = Color.White, modifier = Modifier.weight(1f))
      }
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AdminStatCard(title = "Pending Orders", value = "$pendingCount", icon = Icons.Default.HourglassEmpty, color = OrangeWarning, modifier = Modifier.weight(1f))
        AdminStatCard(title = "Delivered", value = "$deliveredCount", icon = Icons.Default.CheckCircle, color = GreenStock, modifier = Modifier.weight(1f))
      }
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AdminStatCard(title = "Active Products", value = "$productsCount", icon = Icons.Default.ShoppingBag, color = Color.White, modifier = Modifier.weight(1f))
        AdminStatCard(title = "Low / Out of Stock", value = "$lowStockCount", icon = Icons.Default.Warning, color = RedDiscount, modifier = Modifier.weight(1f))
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Quick Operations", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 14.sp)
          Button(
            onClick = { onTabSelect(3) }, // Orders tab
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Process Pending Orders ($pendingCount)", fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { onTabSelect(4) }, // Inventory tab
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Manage Stock & Restock Alerts", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun AdminStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = BlackCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
      Text(title, color = TextSecondaryDark, fontSize = 11.sp)
      Text(value, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 16.sp)
    }
  }
}

@Composable
fun AdminProductsView(
  products: List<Product>,
  onAddProduct: () -> Unit,
  onEditProduct: (Product) -> Unit,
  onDeleteProduct: (Long) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Product Catalog (${products.size})", fontWeight = FontWeight.Bold, color = Color.White)
          Text("Manage items, stock, and multiple images", color = TextSecondaryDark, fontSize = 11.sp)
        }
        Button(onClick = onAddProduct, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Product", fontWeight = FontWeight.Bold)
        }
      }
    }

    items(products, key = { it.id }) { product ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Product Thumbnail
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(BlackSurfaceVariant)
              .border(1.dp, BlackCardBorder, RoundedCornerShape(8.dp))
          ) {
            ProductImageView(
              imageSource = product.images.firstOrNull().orEmpty(),
              contentDescription = product.name,
              modifier = Modifier.fillMaxSize()
            )
          }

          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Text("SKU: ${product.sku} | Cat: ${product.categoryId} | Stock: ${product.stock}", color = TextSecondaryDark, fontSize = 11.sp)
            Text(CurrencyFormatter.format(product.currentPrice), fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 12.sp)
          }

          Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = { onEditProduct(product) }) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Product", tint = GoldPrimary)
            }
            IconButton(onClick = { onDeleteProduct(product.id) }) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Product", tint = RedDiscount)
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminCategoriesView(
  categories: List<Category>,
  viewModel: ShopViewModel,
  onAddCategory: () -> Unit,
  onEditCategory: (Category) -> Unit,
  onDeleteCategory: (String) -> Unit
) {
  val context = LocalContext.current
  var categoryForUpload by remember { mutableStateOf<Category?>(null) }
  val isUploading by viewModel.isUploadingImage.collectAsState()
  val uploadMsg by viewModel.uploadMessage.collectAsState()
  val uploadErr by viewModel.uploadError.collectAsState()

  val categoryImagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    uri?.let {
      categoryForUpload?.let { cat ->
        viewModel.uploadImage(it, context, folder = "categories") { uploadedUrl ->
          viewModel.adminSaveCategory(cat.copy(imageResName = uploadedUrl))
          categoryForUpload = null
        }
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Category Management (${categories.size})", fontWeight = FontWeight.Bold, color = Color.White)
          Text("Upload and customize category images across store", color = TextSecondaryDark, fontSize = 11.sp)
        }
        Button(
          onClick = onAddCategory,
          colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Category", fontWeight = FontWeight.Bold)
        }
      }
    }

    if (isUploading) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = BlackSurfaceVariant)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
            Text(uploadMsg ?: "Uploading category image...", color = GoldPrimary, fontSize = 12.sp)
          }
        }
      }
    }

    if (uploadErr != null) {
      item {
        Text(uploadErr ?: "", color = RedDiscount, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    items(categories, key = { it.id }) { category ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Category Avatar with quick upload overlay
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .border(1.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape)
              .clickable {
                categoryForUpload = category
                categoryImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
              },
            contentAlignment = Alignment.Center
          ) {
            ProductImageView(
              imageSource = category.imageResName,
              contentDescription = category.name,
              modifier = Modifier.fillMaxSize()
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Upload Image",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(category.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Text("Slug: ${category.id} | Items: ${category.productCount}", color = TextSecondaryDark, fontSize = 11.sp)
            Text(
              text = if (category.imageResName.startsWith("http") || category.imageResName.startsWith("file")) "Custom Image Active" else "Preset Icon",
              color = if (category.imageResName.startsWith("http") || category.imageResName.startsWith("file")) GoldPrimary else TextMutedDark,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            // Upload or Replace Image
            IconButton(
              onClick = {
                categoryForUpload = category
                categoryImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
              }
            ) {
              Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Upload or Replace Category Image", tint = GoldLight)
            }

            // Remove/Reset Custom Image
            if (category.imageResName.startsWith("http") || category.imageResName.startsWith("file") || category.imageResName.startsWith("/")) {
              IconButton(
                onClick = {
                  val resetRes = when (category.id) {
                    "men" -> "cat_men_wear_1787981550518"
                    "women" -> "cat_women_wear_1787981564444"
                    "home_textile" -> "cat_home_textile_1787981585422"
                    "crockery" -> "cat_crockery_set_1787981622532"
                    "cultural" -> "cat_cultural_decor_1787981608475"
                    "gadgets" -> "cat_gadgets_watch_1787981639819"
                    else -> ""
                  }
                  viewModel.adminSaveCategory(category.copy(imageResName = resetRes))
                }
              ) {
                Icon(imageVector = Icons.Default.HideImage, contentDescription = "Reset Category Image", tint = OrangeWarning)
              }
            }

            // Edit Category
            IconButton(onClick = { onEditCategory(category) }) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Category", tint = GoldPrimary)
            }

            // Delete Category
            IconButton(onClick = { onDeleteCategory(category.id) }) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Category", tint = RedDiscount)
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminOrdersView(
  orders: List<CustomerOrder>,
  onUpdateStatus: (Long, OrderStatus) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text("Manage Customer Orders (${orders.size})", fontWeight = FontWeight.Bold, color = Color.White)
    }

    items(orders, key = { it.id }) { order ->
      var expanded by remember { mutableStateOf(false) }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(order.orderNumber, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
              Text("${order.customerName} (${order.phone}) • ${order.city}", color = TextSecondaryDark, fontSize = 12.sp)
            }
            Text(CurrencyFormatter.format(order.total), fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 14.sp)
          }

          Divider(color = BlackDivider)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Current Status: ${order.orderStatus.displayName}", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Box {
              Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = BlackSurfaceVariant, contentColor = GoldPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Text("Change Status", fontSize = 11.sp)
              }

              DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(BlackSurface)
              ) {
                OrderStatus.values().forEach { st ->
                  DropdownMenuItem(
                    text = { Text(st.displayName, color = Color.White) },
                    onClick = {
                      onUpdateStatus(order.id, st)
                      expanded = false
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

@Composable
fun AdminInventoryView(
  products: List<Product>,
  onUpdateStock: (Long, Int) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text("Inventory & Stock Level Controller", fontWeight = FontWeight.Bold, color = Color.White)
    }

    items(products, key = { it.id }) { product ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(product.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
            Text("SKU: ${product.sku}", color = TextSecondaryDark, fontSize = 11.sp)
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = { if (product.stock > 0) onUpdateStock(product.id, product.stock - 1) },
              modifier = Modifier.size(28.dp).background(BlackSurfaceVariant, RoundedCornerShape(4.dp))
            ) {
              Icon(imageVector = Icons.Default.Remove, contentDescription = "-", tint = Color.White, modifier = Modifier.size(14.dp))
            }

            Text(
              text = "${product.stock}",
              fontWeight = FontWeight.Bold,
              color = if (product.stock <= product.lowStockThreshold) RedDiscount else Color.White,
              modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
              onClick = { onUpdateStock(product.id, product.stock + 1) },
              modifier = Modifier.size(28.dp).background(BlackSurfaceVariant, RoundedCornerShape(4.dp))
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "+", tint = Color.White, modifier = Modifier.size(14.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminCouponsView(
  coupons: List<Coupon>,
  onAddCoupon: () -> Unit,
  onDeleteCoupon: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Discount Coupons (${coupons.size})", fontWeight = FontWeight.Bold, color = Color.White)
        Button(onClick = onAddCoupon, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Coupon", fontWeight = FontWeight.Bold)
        }
      }
    }

    items(coupons) { coupon ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(coupon.code, fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 14.sp)
            Text(
              text = "Discount: ${if (coupon.discountType == DiscountType.PERCENTAGE) "${coupon.discountValue}%" else "Rs. ${coupon.discountValue}"} | Min: Rs. ${coupon.minOrderAmount}",
              color = TextSecondaryDark,
              fontSize = 11.sp
            )
          }

          IconButton(onClick = { onDeleteCoupon(coupon.code) }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedDiscount)
          }
        }
      }
    }
  }
}

@Composable
fun AdminSettingsView(
  settings: AppSettings,
  onSaveSettings: (AppSettings) -> Unit
) {
  var whatsapp by remember(settings) { mutableStateOf(settings.whatsappNumber) }
  var email by remember(settings) { mutableStateOf(settings.supportEmail) }
  var deliveryFee by remember(settings) { mutableStateOf(settings.defaultDeliveryFee.toString()) }
  var freeThreshold by remember(settings) { mutableStateOf(settings.freeDeliveryThreshold.toString()) }
  var savedSuccess by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text("Store & Contact Settings", fontWeight = FontWeight.Bold, color = Color.White)
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("Official WhatsApp Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Support Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = deliveryFee,
            onValueChange = { deliveryFee = it },
            label = { Text("Standard Delivery Fee (PKR)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = freeThreshold,
            onValueChange = { freeThreshold = it },
            label = { Text("Free Delivery Threshold (PKR)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Button(
            onClick = {
              onSaveSettings(
                settings.copy(
                  whatsappNumber = whatsapp,
                  supportEmail = email,
                  defaultDeliveryFee = deliveryFee.toDoubleOrNull() ?: 200.0,
                  freeDeliveryThreshold = freeThreshold.toDoubleOrNull() ?: 3000.0
                )
              )
              savedSuccess = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Save Settings", fontWeight = FontWeight.Bold)
          }

          if (savedSuccess) {
            Text("Settings saved successfully!", color = GreenStock, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
  initialProduct: Product? = null,
  categories: List<Category>,
  viewModel: ShopViewModel,
  onDismiss: () -> Unit,
  onSave: (Product) -> Unit
) {
  val context = LocalContext.current
  val isEditMode = initialProduct != null

  var name by remember { mutableStateOf(initialProduct?.name ?: "") }
  var sku by remember { mutableStateOf(initialProduct?.sku ?: "") }
  var price by remember { mutableStateOf(initialProduct?.price?.let { if (it > 0) it.toLong().toString() else "" } ?: "") }
  var salePrice by remember { mutableStateOf(initialProduct?.salePrice?.let { if (it > 0) it.toLong().toString() else "" } ?: "") }
  var stock by remember { mutableStateOf((initialProduct?.stock ?: 10).toString()) }
  var selectedCategory by remember { mutableStateOf(initialProduct?.categoryId ?: categories.firstOrNull()?.id ?: "men") }
  var description by remember { mutableStateOf(initialProduct?.description ?: "") }
  var images by remember { mutableStateOf(initialProduct?.images ?: listOf("hero_banner_luxury_1787981532790")) }
  var imageIndexToReplace by remember { mutableStateOf<Int?>(null) }
  var manualUrl by remember { mutableStateOf("") }
  var formError by remember { mutableStateOf<String?>(null) }

  val isUploading by viewModel.isUploadingImage.collectAsState()
  val uploadMsg by viewModel.uploadMessage.collectAsState()
  val uploadErr by viewModel.uploadError.collectAsState()

  // Android Photo Picker for replacing an existing image
  val replaceImagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    uri?.let {
      val targetIndex = imageIndexToReplace
      if (targetIndex != null) {
        viewModel.uploadImage(it, context, folder = "products") { uploadedUrl ->
          if (targetIndex in images.indices) {
            val list = images.toMutableList()
            list[targetIndex] = uploadedUrl
            images = list
          }
          imageIndexToReplace = null
        }
      }
    }
  }

  // Android Photo Picker for single image addition
  val singleImagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    uri?.let {
      viewModel.uploadImage(it, context, folder = "products") { uploadedUrl ->
        images = (images + uploadedUrl).distinct()
      }
    }
  }

  // Android Photo Picker for multiple image addition
  val multipleImagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
  ) { uris ->
    uris.forEach { uri ->
      viewModel.uploadImage(uri, context, folder = "products") { uploadedUrl ->
        images = (images + uploadedUrl).distinct()
      }
    }
  }

  AlertDialog(
    onDismissRequest = {
      viewModel.clearUploadStatus()
      onDismiss()
    },
    containerColor = BlackSurface,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Add,
          contentDescription = null,
          tint = GoldPrimary
        )
        Text(
          text = if (isEditMode) "Edit Product" else "Add New Product",
          color = GoldPrimary,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it; formError = null },
            label = { Text("Product Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = sku,
            onValueChange = { sku = it },
            label = { Text("SKU Code (e.g. AC-MEN-005) *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = price,
              onValueChange = { price = it; formError = null },
              label = { Text("Price (PKR) *") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = salePrice,
              onValueChange = { salePrice = it },
              label = { Text("Sale Price") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
        }

        item {
          OutlinedTextField(
            value = stock,
            onValueChange = { stock = it },
            label = { Text("Stock Quantity") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Category selection chips
        item {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Category *", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(categories) { cat ->
                val isSel = selectedCategory == cat.id
                FilterChip(
                  selected = isSel,
                  onClick = { selectedCategory = cat.id },
                  label = { Text(cat.name, fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = BlackMain,
                    containerColor = BlackSurfaceVariant,
                    labelColor = Color.White
                  )
                )
              }
            }
          }
        }

        // Product Images Gallery Section
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlackCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Product Images (${images.size})", fontWeight = FontWeight.Bold, color = GoldLight, fontSize = 12.sp)
                if (images.isNotEmpty()) {
                  TextButton(
                    onClick = { images = emptyList() },
                    contentPadding = PaddingValues(0.dp)
                  ) {
                    Text("Clear All", color = RedDiscount, fontSize = 10.sp)
                  }
                }
              }

              // Image Preview Carousel with Replace & Delete actions
              if (images.isNotEmpty()) {
                LazyRow(
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                  itemsIndexed(images) { index, img ->
                    Box(
                      modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BlackSurfaceVariant)
                        .border(
                          width = if (index == 0) 2.dp else 1.dp,
                          color = if (index == 0) GoldPrimary else BlackCardBorder,
                          shape = RoundedCornerShape(10.dp)
                        )
                    ) {
                      ProductImageView(
                        imageSource = img,
                        contentDescription = "Image ${index + 1}",
                        modifier = Modifier.fillMaxSize()
                      )

                      // Primary Badge on main image
                      if (index == 0) {
                        Surface(
                          color = GoldPrimary,
                          shape = RoundedCornerShape(bottomEnd = 6.dp),
                          modifier = Modifier.align(Alignment.TopStart)
                        ) {
                          Text(
                            text = "MAIN",
                            color = BlackMain,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                          )
                        }
                      }

                      // Action bar overlay (Replace, Make Main, Delete)
                      Row(
                        modifier = Modifier
                          .align(Alignment.BottomCenter)
                          .fillMaxWidth()
                          .background(Color.Black.copy(alpha = 0.75f))
                          .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        // Replace button
                        IconButton(
                          onClick = {
                            imageIndexToReplace = index
                            replaceImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                          },
                          modifier = Modifier.size(20.dp)
                        ) {
                          Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Replace Image",
                            tint = GoldPrimary,
                            modifier = Modifier.size(13.dp)
                          )
                        }

                        // Make primary (if not already main)
                        if (index != 0) {
                          IconButton(
                            onClick = {
                              val list = images.toMutableList()
                              val item = list.removeAt(index)
                              list.add(0, item)
                              images = list
                            },
                            modifier = Modifier.size(20.dp)
                          ) {
                            Icon(
                              imageVector = Icons.Default.Star,
                              contentDescription = "Set Primary",
                              tint = GoldWarm,
                              modifier = Modifier.size(13.dp)
                            )
                          }
                        }

                        // Remove button
                        IconButton(
                          onClick = {
                            images = images.filterIndexed { i, _ -> i != index }
                          },
                          modifier = Modifier.size(20.dp)
                        ) {
                          Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = RedDiscount,
                            modifier = Modifier.size(13.dp)
                          )
                        }
                      }
                    }
                  }
                }
              } else {
                Text(
                  "No images attached. Select from gallery or add image URL.",
                  color = TextMutedDark,
                  fontSize = 11.sp
                )
              }

              // Upload Action Buttons (Multiple & Single Gallery Pickers)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Button(
                  onClick = {
                    multipleImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain),
                  modifier = Modifier.weight(1.2f)
                ) {
                  Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Upload Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                  onClick = {
                    singleImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                  },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                  border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add Single", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              // Validation & Specs hint
              Text(
                text = "✓ Supported: JPEG, PNG, WebP (Max 10MB per image). Saved to persistent storage.",
                color = TextSecondaryDark,
                fontSize = 10.sp
              )

              // Manual URL entry fallback
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                OutlinedTextField(
                  value = manualUrl,
                  onValueChange = { manualUrl = it },
                  placeholder = { Text("https://example.com/image.jpg", fontSize = 10.sp) },
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
                Button(
                  onClick = {
                    if (manualUrl.isNotBlank()) {
                      images = (images + manualUrl.trim()).distinct()
                      manualUrl = ""
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = BlackSurfaceVariant, contentColor = GoldPrimary)
                ) {
                  Text("Add URL", fontSize = 11.sp)
                }
              }

              // Upload status / feedback
              if (isUploading) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
                ) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GoldPrimary, strokeWidth = 2.dp)
                  Text(uploadMsg ?: "Uploading image to persistent storage...", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }

              if (uploadErr != null) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(RedDiscount.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
                ) {
                  Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RedDiscount, modifier = Modifier.size(16.dp))
                  Text(uploadErr ?: "", color = RedDiscount, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              if (uploadMsg != null && !isUploading) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenStock.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
                ) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenStock, modifier = Modifier.size(16.dp))
                  Text(uploadMsg ?: "", color = GreenStock, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Short Description") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }

        if (formError != null) {
          item {
            Text(formError ?: "", color = RedDiscount, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) {
            formError = "Please enter product name."
            return@Button
          }
          val parsedPrice = price.toDoubleOrNull()
          if (parsedPrice == null || parsedPrice <= 0) {
            formError = "Please enter a valid regular price."
            return@Button
          }
          val finalImages = if (images.isNotEmpty()) images else listOf("hero_banner_luxury_1787981532790")

          viewModel.clearUploadStatus()
          onSave(
            Product(
              id = initialProduct?.id ?: 0L,
              supabaseId = initialProduct?.supabaseId.orEmpty(),
              name = name.trim(),
              slug = name.trim().lowercase().replace(" ", "-"),
              sku = sku.ifBlank { "AC-ITEM-${System.currentTimeMillis() % 1000}" }.trim(),
              price = parsedPrice,
              salePrice = salePrice.toDoubleOrNull(),
              stock = stock.toIntOrNull() ?: 10,
              categoryId = selectedCategory,
              description = description.ifBlank { name }.trim(),
              shortDescription = description.ifBlank { name }.trim(),
              images = finalImages
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)
      ) {
        Text(if (isEditMode) "Save Changes" else "Add Product", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = {
        viewModel.clearUploadStatus()
        onDismiss()
      }) {
        Text("Cancel", color = TextSecondaryDark)
      }
    }
  )
}

@Composable
fun CategoryFormDialog(
  initialCategory: Category? = null,
  viewModel: ShopViewModel,
  onDismiss: () -> Unit,
  onSave: (Category) -> Unit
) {
  val context = LocalContext.current
  val isEditMode = initialCategory != null

  var name by remember { mutableStateOf(initialCategory?.name ?: "") }
  var id by remember { mutableStateOf(initialCategory?.id ?: "") }
  var imageResName by remember { mutableStateOf(initialCategory?.imageResName ?: "") }
  var manualUrl by remember { mutableStateOf("") }
  var formError by remember { mutableStateOf<String?>(null) }

  val isUploading by viewModel.isUploadingImage.collectAsState()
  val uploadMsg by viewModel.uploadMessage.collectAsState()
  val uploadErr by viewModel.uploadError.collectAsState()

  val categoryImagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    uri?.let {
      viewModel.uploadImage(it, context, folder = "categories") { uploadedUrl ->
        imageResName = uploadedUrl
      }
    }
  }

  AlertDialog(
    onDismissRequest = {
      viewModel.clearUploadStatus()
      onDismiss()
    },
    containerColor = BlackSurface,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Category,
          contentDescription = null,
          tint = GoldPrimary
        )
        Text(
          text = if (isEditMode) "Edit Category" else "Add New Category",
          color = GoldPrimary,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          OutlinedTextField(
            value = name,
            onValueChange = {
              name = it
              formError = null
              if (!isEditMode && id.isBlank()) {
                id = it.trim().lowercase().replace(" ", "_").replace("'", "")
              }
            },
            label = { Text("Category Name * (e.g. Kids Wear)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("Category Identifier / Slug *") },
            singleLine = true,
            enabled = !isEditMode,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Image Upload Section
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlackCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text("Category Image", fontWeight = FontWeight.Bold, color = GoldLight, fontSize = 12.sp)

              // Image preview circle
              Box(
                modifier = Modifier
                  .size(76.dp)
                  .clip(CircleShape)
                  .background(BlackSurfaceVariant)
                  .border(2.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                if (imageResName.isNotBlank()) {
                  ProductImageView(
                    imageSource = imageResName,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize()
                  )
                } else {
                  Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(36.dp))
                }
              }

              // Upload Buttons
              // Upload / Replace & Remove Buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = {
                    categoryImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (imageResName.isNotBlank()) "Replace Image" else "Upload Image",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                if (imageResName.isNotBlank()) {
                  OutlinedButton(
                    onClick = { imageResName = "" },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDiscount),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedDiscount),
                    modifier = Modifier.weight(0.7f)
                  ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remove", fontSize = 11.sp)
                  }
                }
              }

              // Validation & Specs hint
              Text(
                text = "✓ Supported: JPEG, PNG, WebP (Max 10MB). Saved persistently to database & cloud.",
                color = TextSecondaryDark,
                fontSize = 10.sp
              )

              // Manual URL entry
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                OutlinedTextField(
                  value = manualUrl,
                  onValueChange = { manualUrl = it },
                  placeholder = { Text("https://example.com/cat.jpg", fontSize = 10.sp) },
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
                Button(
                  onClick = {
                    if (manualUrl.isNotBlank()) {
                      imageResName = manualUrl.trim()
                      manualUrl = ""
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = BlackSurfaceVariant, contentColor = GoldPrimary)
                ) {
                  Text("Set URL", fontSize = 11.sp)
                }
              }

              // Upload status / feedback
              if (isUploading) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
                ) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GoldPrimary, strokeWidth = 2.dp)
                  Text(uploadMsg ?: "Uploading category image...", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }

              if (uploadErr != null) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(RedDiscount.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
                ) {
                  Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RedDiscount, modifier = Modifier.size(16.dp))
                  Text(uploadErr ?: "", color = RedDiscount, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              if (uploadMsg != null && !isUploading) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenStock.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
                ) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenStock, modifier = Modifier.size(16.dp))
                  Text(uploadMsg ?: "", color = GreenStock, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        if (formError != null) {
          item {
            Text(formError ?: "", color = RedDiscount, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) {
            formError = "Please enter category name."
            return@Button
          }
          val finalId = if (id.isNotBlank()) id.trim() else name.trim().lowercase().replace(" ", "_")
          viewModel.clearUploadStatus()
          onSave(
            Category(
              id = finalId,
              name = name.trim(),
              slug = finalId,
              imageResName = imageResName.ifBlank { initialCategory?.imageResName ?: "cat_men_wear_1787981550518" },
              productCount = initialCategory?.productCount ?: 0,
              subcategories = initialCategory?.subcategories ?: emptyList(),
              sortOrder = initialCategory?.sortOrder ?: 0,
              isActive = true
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)
      ) {
        Text(if (isEditMode) "Save Changes" else "Add Category", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = {
        viewModel.clearUploadStatus()
        onDismiss()
      }) {
        Text("Cancel", color = TextSecondaryDark)
      }
    }
  )
}

@Composable
fun AddCouponDialog(
  onDismiss: () -> Unit,
  onSave: (Coupon) -> Unit
) {
  var code by remember { mutableStateOf("") }
  var discountValue by remember { mutableStateOf("") }
  var minAmount by remember { mutableStateOf("2000") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = BlackSurface,
    title = { Text("Create Discount Coupon", color = GoldPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Coupon Code (e.g. SAVE15)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = discountValue, onValueChange = { discountValue = it }, label = { Text("Discount Percent (e.g. 15)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = minAmount, onValueChange = { minAmount = it }, label = { Text("Min Order Amount (PKR)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (code.isNotBlank() && discountValue.toDoubleOrNull() != null) {
            onSave(
              Coupon(
                code = code.uppercase().trim(),
                discountType = DiscountType.PERCENTAGE,
                discountValue = discountValue.toDouble(),
                minOrderAmount = minAmount.toDoubleOrNull() ?: 0.0,
                isActive = true
              )
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)
      ) {
        Text("Create Coupon", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
    }
  )
}
