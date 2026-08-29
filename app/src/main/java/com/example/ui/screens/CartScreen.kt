package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
  viewModel: ShopViewModel,
  onProceedToCheckout: () -> Unit,
  onExploreProducts: () -> Unit
) {
  val cartItems by viewModel.cartItems.collectAsState()
  val cartSummary by viewModel.cartSummary.collectAsState()
  val appliedCoupon by viewModel.appliedCouponCode.collectAsState()
  val couponError by viewModel.couponErrorMessage.collectAsState()

  var couponInput by remember { mutableStateOf("") }
  val context = LocalContext.current

  if (cartItems.isEmpty()) {
    EmptyStateView(
      icon = Icons.Outlined.ShoppingBag,
      title = "Your Shopping Cart is Empty",
      subtitle = "Add Shalwar Kameez, bedsheets, luxury curtains, crockery, and gadgets to your cart.",
      actionText = "Start Shopping",
      onActionClick = onExploreProducts
    )
  } else {
    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Total Amount", color = TextSecondaryDark, fontSize = 12.sp)
                Text(
                  text = CurrencyFormatter.format(cartSummary.grandTotal),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.ExtraBold,
                  color = GoldPrimary
                )
              }

              Button(
                onClick = onProceedToCheckout,
                modifier = Modifier
                  .weight(1f)
                  .padding(start = 16.dp)
                  .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = GoldPrimary,
                  contentColor = BlackMain
                )
              ) {
                Text("Checkout", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    ) { paddingValues ->
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. Free Shipping Progress Bar
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlackCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = if (cartSummary.amountNeededForFreeDelivery <= 0) {
                      "🎉 Congratulations! You have unlocked FREE Delivery!"
                    } else {
                      "Add ${CurrencyFormatter.format(cartSummary.amountNeededForFreeDelivery)} more for FREE Delivery"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (cartSummary.amountNeededForFreeDelivery <= 0) GreenStock else Color.White
                  )
                }
              }

              LinearProgressIndicator(
                progress = { cartSummary.freeDeliveryProgress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = GoldPrimary,
                trackColor = BlackSurfaceVariant
              )
            }
          }
        }

        // 2. Cart Items List
        items(cartItems, key = { it.id }) { item ->
          CartItemRow(
            item = item,
            onIncrease = { viewModel.updateCartItemQuantity(item.id, item.quantity + 1) },
            onDecrease = { viewModel.updateCartItemQuantity(item.id, item.quantity - 1) },
            onRemove = { viewModel.removeCartItem(item.id) }
          )
        }

        // 3. Promo Code Card
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlackCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text("Discount Voucher / Coupon", fontWeight = FontWeight.Bold, color = GoldLight, fontSize = 13.sp)

              if (appliedCoupon.isNotBlank()) {
                Surface(
                  color = GoldPrimary.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                      Text("Code '$appliedCoupon' Applied", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Text(
                      text = "Remove",
                      color = RedDiscount,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.clickable { viewModel.removeCoupon() }
                    )
                  }
                }
              } else {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  OutlinedTextField(
                    value = couponInput,
                    onValueChange = { couponInput = it },
                    placeholder = { Text("e.g. ARSHAD10", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = GoldPrimary,
                      unfocusedBorderColor = BlackCardBorder,
                      focusedContainerColor = BlackSurfaceVariant,
                      unfocusedContainerColor = BlackSurfaceVariant,
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White
                    )
                  )

                  Button(
                    onClick = {
                      if (couponInput.isNotBlank()) {
                        viewModel.applyCoupon(couponInput)
                      }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = GoldPrimary,
                      contentColor = BlackMain
                    ),
                    modifier = Modifier.height(52.dp)
                  ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                  }
                }
              }

              if (couponError != null) {
                Text(
                  text = couponError ?: "",
                  color = RedDiscount,
                  fontSize = 11.sp
                )
              }
            }
          }
        }

        // 4. Order Summary Card
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlackCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text("Order Summary", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

              Divider(color = BlackDivider)

              SummaryRow("Items Subtotal", CurrencyFormatter.format(cartSummary.subtotal))

              if (cartSummary.discount > 0) {
                SummaryRow("Coupon Discount", "- ${CurrencyFormatter.format(cartSummary.discount)}", valueColor = GreenStock)
              }

              SummaryRow(
                label = "Delivery Fee (Nationwide)",
                value = if (cartSummary.deliveryFee == 0.0) "FREE" else CurrencyFormatter.format(cartSummary.deliveryFee),
                valueColor = if (cartSummary.deliveryFee == 0.0) GreenStock else Color.White
              )

              Divider(color = BlackDivider)

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Total Amount", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                  text = CurrencyFormatter.format(cartSummary.grandTotal),
                  color = GoldPrimary,
                  fontWeight = FontWeight.ExtraBold,
                  style = MaterialTheme.typography.titleLarge
                )
              }
            }
          }
        }

        // 5. WhatsApp Order Alternative
        item {
          OutlinedButton(
            onClick = {
              val itemsText = cartItems.joinToString("\n") {
                "• ${it.product.name} (Qty: ${it.quantity}${if (it.selectedSize.isNotBlank()) ", Size: ${it.selectedSize}" else ""}) - ${CurrencyFormatter.format(it.itemTotal)}"
              }
              val msg = WhatsAppHelper.createCartOrderMessage(itemsText, cartSummary.grandTotal)
              WhatsAppHelper.openWhatsApp(context, msg)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppGreen)
          ) {
            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Order via WhatsApp (03413399629)", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun CartItemRow(
  item: CartItem,
  onIncrease: () -> Unit,
  onDecrease: () -> Unit,
  onRemove: () -> Unit
) {
  val context = LocalContext.current
  val firstImg = item.product.images.firstOrNull() ?: ""
  val imageResId = remember(firstImg) {
    if (firstImg.isNotBlank()) {
      context.resources.getIdentifier(firstImg, "drawable", context.packageName)
    } else 0
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = BlackCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Product Image
      Box(
        modifier = Modifier
          .size(76.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(BlackSurfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        if (imageResId != 0) {
          Image(
            painter = painterResource(id = imageResId),
            contentDescription = item.product.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        } else {
          Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = GoldPrimary)
        }
      }

      // Details
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = item.product.name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        if (item.selectedSize.isNotBlank() || item.selectedColor.isNotBlank()) {
          Text(
            text = "Variant: ${item.selectedSize}${if (item.selectedColor.isNotBlank()) " / ${item.selectedColor}" else ""}",
            fontSize = 11.sp,
            color = TextSecondaryDark
          )
        }

        Text(
          text = CurrencyFormatter.format(item.product.currentPrice),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = GoldPrimary
        )

        // Quantity controls
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          IconButton(
            onClick = onDecrease,
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(BlackSurfaceVariant)
          ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(14.dp))
          }

          Text("${item.quantity}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)

          IconButton(
            onClick = onIncrease,
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(BlackSurfaceVariant)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(14.dp))
          }
        }
      }

      // Remove button
      IconButton(onClick = onRemove) {
        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Remove", tint = RedDiscount.copy(alpha = 0.8f))
      }
    }
  }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color = Color.White) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, color = TextSecondaryDark, fontSize = 13.sp)
    Text(value, color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
  }
}
