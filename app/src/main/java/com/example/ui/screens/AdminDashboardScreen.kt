package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        listOf("Overview", "Products", "Orders", "Inventory", "Coupons", "Store Settings").forEachIndexed { index, title ->
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
          onDeleteProduct = { viewModel.adminDeleteProduct(it) }
        )
        2 -> AdminOrdersView(
          orders = allOrders,
          onUpdateStatus = { id, st -> viewModel.adminUpdateOrderStatus(id, st) }
        )
        3 -> AdminInventoryView(
          products = allProducts,
          onUpdateStock = { id, stock -> viewModel.adminUpdateStock(id, stock) }
        )
        4 -> AdminCouponsView(
          coupons = allCoupons,
          onAddCoupon = { showAddCouponDialog = true },
          onDeleteCoupon = { viewModel.adminDeleteCoupon(it) }
        )
        5 -> AdminSettingsView(
          settings = appSettings,
          onSaveSettings = { viewModel.adminUpdateSettings(it) }
        )
      }
    }
  }

  // Add Product Dialog
  if (showAddProductDialog) {
    AddProductDialog(
      categories = allCategories,
      onDismiss = { showAddProductDialog = false },
      onSave = { newProd ->
        viewModel.adminSaveProduct(newProd)
        showAddProductDialog = false
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
            onClick = { onTabSelect(2) }, // Orders tab
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Process Pending Orders ($pendingCount)", fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { onTabSelect(3) }, // Inventory tab
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
        Text("Product Catalog (${products.size})", fontWeight = FontWeight.Bold, color = Color.White)
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
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Text("SKU: ${product.sku} | Cat: ${product.categoryId} | Stock: ${product.stock}", color = TextSecondaryDark, fontSize = 11.sp)
            Text(CurrencyFormatter.format(product.currentPrice), fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 12.sp)
          }

          IconButton(onClick = { onDeleteProduct(product.id) }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedDiscount)
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

@Composable
fun AddProductDialog(
  categories: List<Category>,
  onDismiss: () -> Unit,
  onSave: (Product) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var sku by remember { mutableStateOf("") }
  var price by remember { mutableStateOf("") }
  var salePrice by remember { mutableStateOf("") }
  var stock by remember { mutableStateOf("10") }
  var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.id ?: "men") }
  var description by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = BlackSurface,
    title = { Text("Add New Product", color = GoldPrimary, fontWeight = FontWeight.Bold) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
          OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU Code (e.g. AC-MEN-005) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
          OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Regular Price (PKR) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
          OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Sale Price (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
          OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Initial Stock Qty") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
          OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Short Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && price.toDoubleOrNull() != null) {
            onSave(
              Product(
                name = name,
                slug = name.lowercase().replace(" ", "-"),
                sku = sku.ifBlank { "AC-ITEM-${System.currentTimeMillis() % 1000}" },
                price = price.toDouble(),
                salePrice = salePrice.toDoubleOrNull(),
                stock = stock.toIntOrNull() ?: 10,
                categoryId = selectedCategory,
                description = description.ifBlank { name },
                shortDescription = description.ifBlank { name },
                images = listOf("hero_banner_luxury_1787981532790")
              )
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)
      ) {
        Text("Save Product", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
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
