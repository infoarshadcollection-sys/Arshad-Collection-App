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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerOrder
import com.example.data.model.OrderStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OrderStatusTimeline
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(
  viewModel: ShopViewModel,
  onOrderClick: (CustomerOrder) -> Unit,
  onExploreProducts: () -> Unit
) {
  val orders by viewModel.customerOrders.collectAsState()

  if (orders.isEmpty()) {
    EmptyStateView(
      icon = Icons.Outlined.LocalShipping,
      title = "No Orders Placed Yet",
      subtitle = "Your order history, live parcel tracking, and invoices will appear here.",
      actionText = "Shop Arshad Collection",
      onActionClick = onExploreProducts
    )
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(orders, key = { it.id }) { order ->
        OrderHistoryCard(
          order = order,
          onClick = { onOrderClick(order) }
        )
      }
    }
  }
}

@Composable
fun OrderHistoryCard(
  order: CustomerOrder,
  onClick: () -> Unit
) {
  val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH) }
  val formattedDate = remember(order.createdAt) { dateFormatter.format(Date(order.createdAt)) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = BlackCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(order.orderNumber, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
          Text(formattedDate, color = TextMutedDark, fontSize = 11.sp)
        }

        // Status badge
        Surface(
          color = when (order.orderStatus) {
            OrderStatus.DELIVERED -> GreenStock.copy(alpha = 0.2f)
            OrderStatus.CANCELLED, OrderStatus.RETURNED -> RedDiscount.copy(alpha = 0.2f)
            else -> GoldPrimary.copy(alpha = 0.2f)
          },
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (order.orderStatus) {
              OrderStatus.DELIVERED -> GreenStock
              OrderStatus.CANCELLED, OrderStatus.RETURNED -> RedDiscount
              else -> GoldPrimary
            }
          )
        ) {
          Text(
            text = order.orderStatus.displayName,
            color = when (order.orderStatus) {
              OrderStatus.DELIVERED -> GreenStock
              OrderStatus.CANCELLED, OrderStatus.RETURNED -> RedDiscount
              else -> GoldPrimary
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Divider(color = BlackDivider)

      Text(
        text = "${order.items.size} item(s) • ${order.city} • Total: ${CurrencyFormatter.format(order.total)}",
        color = TextSecondaryDark,
        fontSize = 12.sp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Track Details & Receipt", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
  viewModel: ShopViewModel,
  onBackClick: () -> Unit
) {
  val order = viewModel.selectedOrder.collectAsState().value
  val context = LocalContext.current

  if (order == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(BlackMain),
      contentAlignment = Alignment.Center
    ) {
      Text("Order not found", color = Color.White)
    }
    return
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Order #${order.orderNumber}", color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackSurface)
      )
    },
    bottomBar = {
      Surface(
        color = BlackSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              val msg = WhatsAppHelper.createOrderInquiryMessage(order.orderNumber, order.total)
              WhatsAppHelper.openWhatsApp(context, msg)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = WhatsAppGreen,
              contentColor = Color.White
            )
          ) {
            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Inquire on WhatsApp (03413399629)", fontWeight = FontWeight.Bold)
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
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Order Status Timeline Card
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
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Delivery Progress", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Text("Tracking: ${order.trackingNumber}", color = TextSecondaryDark, fontSize = 12.sp)
            }

            Divider(color = BlackDivider)

            OrderStatusTimeline(currentStatus = order.orderStatus)
          }
        }
      }

      // 2. Shipping Details Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = BlackCard),
          border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("Delivery Information", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Divider(color = BlackDivider)
            SummaryRow("Recipient", order.customerName)
            SummaryRow("Contact", order.phone)
            SummaryRow("City", order.city)
            SummaryRow("Address", order.shippingAddress)
            if (order.deliveryInstructions.isNotBlank()) {
              SummaryRow("Notes", order.deliveryInstructions)
            }
          }
        }
      }

      // 3. Ordered Items List
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
            Text("Items in this Package (${order.items.size})", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Divider(color = BlackDivider)

            order.items.forEach { item ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(item.productName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                  Text("Qty: ${item.quantity}${if (item.size.isNotBlank()) " | Size: ${item.size}" else ""}", color = TextSecondaryDark, fontSize = 11.sp)
                }
                Text(CurrencyFormatter.format(item.totalPrice), color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
              Text("Total Paid / Payable", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text(CurrencyFormatter.format(order.total), color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
          }
        }
      }
    }
  }
}
