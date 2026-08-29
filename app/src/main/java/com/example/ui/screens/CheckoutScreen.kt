package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArshadTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
  viewModel: ShopViewModel,
  onBackClick: () -> Unit
) {
  val checkoutForm by viewModel.checkoutForm.collectAsState()
  val cartItems by viewModel.cartItems.collectAsState()
  val cartSummary by viewModel.cartSummary.collectAsState()
  val isPlacingOrder by viewModel.isPlacingOrder.collectAsState()
  val orderError by viewModel.orderPlacementError.collectAsState()

  // Major Pakistani Cities
  val pakistaniCities = listOf(
    "Karachi", "Lahore", "Islamabad", "Rawalpindi", "Faisalabad",
    "Multan", "Peshawar", "Quetta", "Sialkot", "Gujranwala",
    "Hyderabad", "Bahawalpur", "Sargodha", "Abbottabad", "Other City"
  )

  var cityDropdownExpanded by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      ArshadTopBar(
        title = "Checkout",
        showBackButton = true,
        onBackClick = onBackClick
      )
    },
    bottomBar = {
      Surface(
        color = BlackSurface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (orderError != null) {
            Text(
              text = orderError ?: "",
              color = RedDiscount,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Button(
            onClick = { viewModel.placeOrder() },
            enabled = !isPlacingOrder,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = BlackMain
            )
          ) {
            if (isPlacingOrder) {
              CircularProgressIndicator(color = BlackMain, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Placing Order...", fontWeight = FontWeight.Bold)
            } else {
              Text(
                text = "Place Order • ${CurrencyFormatter.format(cartSummary.grandTotal)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Delivery Contact Information
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = BlackCard),
          border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GoldPrimary)
              Text("Customer & Contact Information", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Divider(color = BlackDivider)

            OutlinedTextField(
              value = checkoutForm.fullName,
              onValueChange = { viewModel.checkoutForm.value = checkoutForm.copy(fullName = it) },
              label = { Text("Full Name *") },
              placeholder = { Text("e.g. Arshad Ahmed") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = checkoutForm.phone,
              onValueChange = { viewModel.checkoutForm.value = checkoutForm.copy(phone = it) },
              label = { Text("Mobile / WhatsApp Number *") },
              placeholder = { Text("0341-3399629") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = checkoutForm.email,
              onValueChange = { viewModel.checkoutForm.value = checkoutForm.copy(email = it) },
              label = { Text("Email Address (Optional)") },
              placeholder = { Text("info.arshadcollection@gmail.com") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // 2. Shipping Address in Pakistan
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = BlackCard),
          border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GoldPrimary)
              Text("Shipping Address (Pakistan)", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Divider(color = BlackDivider)

            // City Picker Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
              OutlinedTextField(
                value = checkoutForm.city,
                onValueChange = {},
                readOnly = true,
                label = { Text("City *") },
                trailingIcon = {
                  IconButton(onClick = { cityDropdownExpanded = true }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select City", tint = GoldPrimary)
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { cityDropdownExpanded = true }
              )

              DropdownMenu(
                expanded = cityDropdownExpanded,
                onDismissRequest = { cityDropdownExpanded = false },
                modifier = Modifier.background(BlackSurface)
              ) {
                pakistaniCities.forEach { city ->
                  DropdownMenuItem(
                    text = { Text(city, color = Color.White) },
                    onClick = {
                      viewModel.checkoutForm.value = checkoutForm.copy(city = city)
                      cityDropdownExpanded = false
                    }
                  )
                }
              }
            }

            OutlinedTextField(
              value = checkoutForm.area,
              onValueChange = { viewModel.checkoutForm.value = checkoutForm.copy(area = it) },
              label = { Text("Area / Sector / Colony") },
              placeholder = { Text("e.g. Gulshan-e-Iqbal, DHA Phase 5, Model Town") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = checkoutForm.address,
              onValueChange = { viewModel.checkoutForm.value = checkoutForm.copy(address = it) },
              label = { Text("Complete Street Address / House No. *") },
              placeholder = { Text("House # 12, Street 4, Near Landmark") },
              minLines = 2,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = checkoutForm.deliveryInstructions,
              onValueChange = { viewModel.checkoutForm.value = checkoutForm.copy(deliveryInstructions = it) },
              label = { Text("Special Delivery Notes / Timings (Optional)") },
              placeholder = { Text("e.g. Call before delivery, deliver after 2 PM") },
              minLines = 2,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // 3. Payment Method
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = BlackCard),
          border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = GoldPrimary)
              Text("Payment Method", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Divider(color = BlackDivider)

            // Cash on Delivery option
            PaymentOptionRow(
              title = "Cash on Delivery (COD)",
              subtitle = "Pay cash in PKR when courier arrives at your doorstep.",
              isSelected = checkoutForm.paymentMethod == "Cash on Delivery",
              onSelect = { viewModel.checkoutForm.value = checkoutForm.copy(paymentMethod = "Cash on Delivery") }
            )

            // Direct Bank Transfer
            PaymentOptionRow(
              title = "Direct Bank Transfer / JazzCash / EasyPaisa",
              subtitle = "Transfer directly to business account. Screenshot confirmed via WhatsApp.",
              isSelected = checkoutForm.paymentMethod == "Direct Bank Transfer",
              onSelect = { viewModel.checkoutForm.value = checkoutForm.copy(paymentMethod = "Direct Bank Transfer") }
            )
          }
        }
      }

      // 4. Order Review
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = BlackCard),
          border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text("Order Breakdown (${cartItems.size} items)", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

            cartItems.forEach { item ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("${item.quantity}x ${item.product.name}", color = TextSecondaryDark, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
                Text(CurrencyFormatter.format(item.itemTotal), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }

            Divider(color = BlackDivider)

            SummaryRow("Subtotal", CurrencyFormatter.format(cartSummary.subtotal))
            if (cartSummary.discount > 0) {
              SummaryRow("Discount", "- ${CurrencyFormatter.format(cartSummary.discount)}", valueColor = GreenStock)
            }
            SummaryRow("Delivery Fee", if (cartSummary.deliveryFee == 0.0) "FREE" else CurrencyFormatter.format(cartSummary.deliveryFee), valueColor = if (cartSummary.deliveryFee == 0.0) GreenStock else Color.White)

            Divider(color = BlackDivider)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Total Payable", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text(CurrencyFormatter.format(cartSummary.grandTotal), color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun PaymentOptionRow(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  Surface(
    onClick = onSelect,
    shape = RoundedCornerShape(8.dp),
    color = if (isSelected) GoldPrimary.copy(alpha = 0.12f) else BlackSurfaceVariant,
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldPrimary else BlackCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      RadioButton(
        selected = isSelected,
        onClick = onSelect,
        colors = RadioButtonDefaults.colors(
          selectedColor = GoldPrimary,
          unselectedColor = TextSecondaryDark
        )
      )

      Column {
        Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
        Text(subtitle, color = TextSecondaryDark, fontSize = 11.sp, lineHeight = 15.sp)
      }
    }
  }
}
