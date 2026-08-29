package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArshadBrandLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.WhatsAppHelper

@Composable
fun AccountScreen(
  viewModel: ShopViewModel,
  onNavigateToOrders: () -> Unit,
  onNavigateToWishlist: () -> Unit,
  onNavigateToAdmin: () -> Unit
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
  val wishlistItems by viewModel.wishlistItems.collectAsState()
  val customerOrders by viewModel.customerOrders.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val appSettings by viewModel.appSettings.collectAsState()

  val context = LocalContext.current

  var showNotificationsDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }
  var showPolicyDialog by remember { mutableStateOf(false) }
  var showFaqDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Profile / Account Header Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            ArshadBrandLogo(size = 54)

            Column(modifier = Modifier.weight(1f)) {
              if (isUserLoggedIn && currentUser != null) {
                Text(
                  text = currentUser!!.username,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 17.sp
                )
                Text(
                  text = currentUser!!.email,
                  color = GoldPrimary,
                  fontSize = 13.sp
                )
                Text(
                  text = if (currentUser!!.role.equals("admin", ignoreCase = true)) "Verified Administrator • Store Manager" else "Member • Style • Quality • Trust",
                  color = if (currentUser!!.role.equals("admin", ignoreCase = true)) GoldPrimary else TextSecondaryDark,
                  fontSize = 11.sp,
                  fontWeight = if (currentUser!!.role.equals("admin", ignoreCase = true)) FontWeight.SemiBold else FontWeight.Normal
                )
              } else {
                Text(
                  text = "Guest Shopper",
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 16.sp
                )
                Text(
                  text = "Style • Quality • Trust",
                  color = GoldPrimary,
                  fontSize = 12.sp
                )
                Text(
                  text = "Delivery across Pakistan 🇵🇰",
                  color = TextSecondaryDark,
                  fontSize = 11.sp
                )
              }
            }
          }

          // Auth Action Button (Login / Register if guest, Logout if logged in)
          if (isUserLoggedIn && currentUser != null) {
            OutlinedButton(
              onClick = { viewModel.logout() },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDiscount),
              border = androidx.compose.foundation.BorderStroke(1.dp, RedDiscount.copy(alpha = 0.6f)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Logout",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Logout", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
          } else {
            Button(
              onClick = { viewModel.currentScreen.value = "login" },
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = BlackMain
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Outlined.Login,
                contentDescription = "Login",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Login / Register", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
      }
    }

    // 2. Quick Action Grid (Orders, Wishlist, Notifications)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickStatCard(
          title = "My Orders",
          subtitle = "${customerOrders.size} Placed",
          icon = Icons.Outlined.LocalShipping,
          onClick = onNavigateToOrders,
          modifier = Modifier.weight(1f)
        )

        QuickStatCard(
          title = "Wishlist",
          subtitle = "${wishlistItems.size} Saved",
          icon = Icons.Outlined.FavoriteBorder,
          onClick = onNavigateToWishlist,
          modifier = Modifier.weight(1f)
        )

        QuickStatCard(
          title = "Alerts",
          subtitle = "${notifications.size} Updates",
          icon = Icons.Outlined.Notifications,
          onClick = { showNotificationsDialog = true },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 3. Customer Support & Contact Section
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
          Text("Help & Customer Care", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Divider(color = BlackDivider)

          // AI Assistant Direct Row
          AccountMenuRow(
            icon = Icons.Default.AutoAwesome,
            iconTint = GoldPrimary,
            title = "Ask Arshad AI Assistant",
            subtitle = "Instant help on sizes, pricing, styling, orders & policies (English, اردو, Roman Urdu)",
            onClick = { viewModel.openAiAssistant() }
          )

          // WhatsApp Direct Button
          AccountMenuRow(
            icon = Icons.Default.Chat,
            iconTint = WhatsAppGreen,
            title = "WhatsApp Support",
            subtitle = appSettings.whatsappNumber,
            onClick = {
              WhatsAppHelper.openWhatsApp(
                context,
                "Assalam-o-Alaikum Arshad Collection, I need customer support regarding my shopping / order."
              )
            }
          )

          // Email Button
          AccountMenuRow(
            icon = Icons.Default.Email,
            iconTint = GoldWarm,
            title = "Official Email",
            subtitle = appSettings.supportEmail,
            onClick = {
              val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${appSettings.supportEmail}")
                putExtra(Intent.EXTRA_SUBJECT, "Customer Inquiry - Arshad Collection")
              }
              try { context.startActivity(intent) } catch (e: Exception) {}
            }
          )

          // FAQ Accordion
          AccountMenuRow(
            icon = Icons.Outlined.Quiz,
            title = "Frequently Asked Questions (FAQ)",
            subtitle = "Payment, delivery timeframes, and exchange queries",
            onClick = { showFaqDialog = true }
          )
        }
      }
    }

    // 4. Store Information & Policies
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
          Text("About & Legal", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Divider(color = BlackDivider)

          AccountMenuRow(
            icon = Icons.Outlined.Info,
            title = "About Arshad Collection",
            subtitle = "Our brand heritage, vision, and craftsmanship",
            onClick = { showAboutDialog = true }
          )

          AccountMenuRow(
            icon = Icons.Outlined.Policy,
            title = "Return, Exchange & Privacy Policy",
            subtitle = "7-day nationwide replacement guarantee",
            onClick = { showPolicyDialog = true }
          )
        }
      }
    }

    // 5. Admin Portal Management Entry (Strictly visible ONLY for authenticated Admin)
    if (isUserLoggedIn && currentUser?.role?.equals("admin", ignoreCase = true) == true) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onNavigateToAdmin() },
          colors = CardDefaults.cardColors(containerColor = BlackSurfaceVariant),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldPrimary)
            }

            Column(modifier = Modifier.weight(1f)) {
              Text("Admin & Merchant Portal", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 14.sp)
              Text("Manage products, inventory, orders & store settings", color = TextSecondaryDark, fontSize = 11.sp)
            }

            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = GoldPrimary)
          }
        }
      }
    }
  }

  // FAQ Dialog
  if (showFaqDialog) {
    AlertDialog(
      onDismissRequest = { showFaqDialog = false },
      containerColor = BlackSurface,
      title = { Text("Frequently Asked Questions", color = GoldPrimary, fontWeight = FontWeight.Bold) },
      text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          item {
            FaqItem("How do I pay?", "We offer Cash on Delivery (COD) across Pakistan. You can also pay via Direct Bank Transfer, JazzCash, or EasyPaisa.")
          }
          item {
            FaqItem("How long does delivery take?", "Standard delivery takes 2 to 4 business days to all major cities in Pakistan (Karachi, Lahore, Islamabad, Faisalabad, Multan, Peshawar, etc.).")
          }
          item {
            FaqItem("Is there free shipping?", "Yes, all orders above Rs. 3,000 qualify for 100% Free Nationwide Delivery!")
          }
          item {
            FaqItem("Can I exchange or return?", "Yes! We offer a 7-day hassle-free exchange policy for any defect or sizing issue.")
          }
        }
      },
      confirmButton = {
        Button(onClick = { showFaqDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)) {
          Text("Close")
        }
      }
    )
  }

  // About Dialog
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      containerColor = BlackSurface,
      title = { Text("About Arshad Collection", color = GoldPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "Arshad Collection is a premium online retail and reselling enterprise founded by Arshad Ahmed. Built on the core values of Style • Quality • Trust, we deliver premium Men's and Women's Eastern apparel, luxury home textiles, authentic Multani cultural handicrafts, crockery, and smart lifestyle gadgets to households across Pakistan.",
            color = TextSecondaryDark,
            fontSize = 13.sp,
            lineHeight = 19.sp
          )
          Text("Founder & Owner: Arshad Ahmed", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text("WhatsApp: 0341-3399629", color = WhatsAppGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text("Email: info.arshadcollection@gmail.com", color = GoldPrimary, fontSize = 12.sp)
        }
      },
      confirmButton = {
        Button(onClick = { showAboutDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)) {
          Text("Close")
        }
      }
    )
  }

  // Policy Dialog
  if (showPolicyDialog) {
    AlertDialog(
      onDismissRequest = { showPolicyDialog = false },
      containerColor = BlackSurface,
      title = { Text("Return & Exchange Policy", color = GoldPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("1. 7-Day Exchange: Any damaged or incorrect item can be exchanged within 7 days of delivery.", color = TextSecondaryDark, fontSize = 13.sp)
          Text("2. Condition: Items must be unused, unwashed, and in original packaging with tags intact.", color = TextSecondaryDark, fontSize = 13.sp)
          Text("3. COD Orders: In cash-on-delivery orders, you pay upon receiving the parcel from the courier rider.", color = TextSecondaryDark, fontSize = 13.sp)
          Text("4. Customer Support: For any return requests, simply send a photo/video on WhatsApp at 0341-3399629.", color = TextSecondaryDark, fontSize = 13.sp)
        }
      },
      confirmButton = {
        Button(onClick = { showPolicyDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)) {
          Text("Understood")
        }
      }
    )
  }

  // Notifications Dialog
  if (showNotificationsDialog) {
    AlertDialog(
      onDismissRequest = { showNotificationsDialog = false },
      containerColor = BlackSurface,
      title = { Text("Notifications & Alerts", color = GoldPrimary, fontWeight = FontWeight.Bold) },
      text = {
        if (notifications.isEmpty()) {
          Text("No notifications at this time.", color = TextSecondaryDark)
        } else {
          LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notifications) { notif ->
              Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(notif.title, color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text(notif.message, color = TextSecondaryDark, fontSize = 12.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(onClick = { showNotificationsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
fun QuickStatCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = BlackCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
      Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
      Text(subtitle, color = TextSecondaryDark, fontSize = 10.sp)
    }
  }
}

@Composable
fun AccountMenuRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  iconTint: Color = GoldPrimary,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(iconTint.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
      Text(subtitle, color = TextSecondaryDark, fontSize = 11.sp)
    }

    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(18.dp))
  }
}

@Composable
fun FaqItem(question: String, answer: String) {
  Card(
    colors = CardDefaults.cardColors(containerColor = BlackCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(question, fontWeight = FontWeight.Bold, color = GoldLight, fontSize = 13.sp)
      Text(answer, color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 16.sp)
    }
  }
}
