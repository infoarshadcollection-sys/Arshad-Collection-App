package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerOrder
import com.example.ui.components.ArshadBrandLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter
import com.example.util.WhatsAppHelper

@Composable
fun OrderConfirmationScreen(
  viewModel: ShopViewModel,
  onContinueShopping: () -> Unit,
  onViewOrders: () -> Unit
) {
  val order = viewModel.lastPlacedOrder.collectAsState().value
  val context = LocalContext.current

  if (order == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(BlackMain),
      contentAlignment = Alignment.Center
    ) {
      Button(onClick = onContinueShopping) {
        Text("Back to Home")
      }
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding(),
    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Success Circle Animation Emblem
    item {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(GreenStock.copy(alpha = 0.15f))
            .border(2.dp, GreenStock, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Success",
            tint = GreenStock,
            modifier = Modifier.size(44.dp)
          )
        }

        Text(
          text = "Shukriya! Order Confirmed",
          style = MaterialTheme.typography.headlineMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          textAlign = TextAlign.Center
        )

        Text(
          text = "Your order #${order.orderNumber} has been received and is being prepared for dispatch.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondaryDark,
          textAlign = TextAlign.Center
        )
      }
    }

    // 2. Receipt Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            ArshadBrandLogo(size = 32)
            Column(horizontalAlignment = Alignment.End) {
              Text("ORDER RECEIPT", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 12.sp)
              Text(order.orderNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }

          Divider(color = BlackDivider)

          SummaryRow("Customer Name", order.customerName)
          SummaryRow("Phone Number", order.phone)
          SummaryRow("Shipping City", order.city)
          SummaryRow("Delivery Address", order.shippingAddress)
          SummaryRow("Tracking Number", order.trackingNumber, valueColor = GoldWarm)
          SummaryRow("Payment Method", order.paymentMethod)

          Divider(color = BlackDivider)

          Text("Purchased Items (${order.items.size}):", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)

          order.items.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${item.quantity}x ${item.productName}${if (item.size.isNotBlank()) " (${item.size})" else ""}",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1
              )
              Text(
                text = CurrencyFormatter.format(item.totalPrice),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Divider(color = BlackDivider)

          SummaryRow("Subtotal", CurrencyFormatter.format(order.subtotal))
          if (order.discount > 0) {
            SummaryRow("Discount", "- ${CurrencyFormatter.format(order.discount)}", valueColor = GreenStock)
          }
          SummaryRow("Delivery Fee", if (order.deliveryFee == 0.0) "FREE" else CurrencyFormatter.format(order.deliveryFee))

          Divider(color = BlackDivider)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Total Amount", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(CurrencyFormatter.format(order.total), color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
          }
        }
      }
    }

    // 3. WhatsApp Order Confirmation Button
    item {
      Button(
        onClick = {
          val msg = WhatsAppHelper.createOrderInquiryMessage(order.orderNumber, order.total)
          WhatsAppHelper.openWhatsApp(context, msg)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = WhatsAppGreen,
          contentColor = Color.White
        )
      ) {
        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Get WhatsApp Order Updates", fontWeight = FontWeight.Bold)
      }
    }

    // 4. Track Order / Continue Shopping
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = {
            viewModel.selectOrder(order)
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
        ) {
          Text("Track Order", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onContinueShopping,
          modifier = Modifier
            .weight(1f)
            .height(48.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = BlackMain
          )
        ) {
          Text("Continue Shopping", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
