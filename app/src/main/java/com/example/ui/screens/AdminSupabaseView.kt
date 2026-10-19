package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.supabase.RealtimeConnectionState
import com.example.data.supabase.SupabaseSyncStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminSupabaseView(
  viewModel: ShopViewModel,
  modifier: Modifier = Modifier
) {
  val syncStatus by viewModel.syncStatus.collectAsState()
  val realtimeState by viewModel.realtimeState.collectAsState()
  val lastSyncTime by viewModel.lastSyncTime.collectAsState()
  val isConfigured by viewModel.isSupabaseConfigured.collectAsState()

  val config = viewModel.repository.supabaseConfig
  val client = viewModel.repository.supabaseClient

  var showEditDialog by remember { mutableStateOf(false) }

  val formattedTime = remember(lastSyncTime) {
    if (lastSyncTime > 0) {
      SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date(lastSyncTime))
    } else "Never"
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CloudSync,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(28.dp)
            )
            Column {
              Text(
                text = "Supabase Backend Integration",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Single Source of Truth for Products & Categories",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
              )
            }
          }

          HorizontalDivider(color = BlackCardBorder)

          // Status Badges
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Configuration Badge
            Surface(
              color = if (isConfigured) GreenStock.copy(alpha = 0.15f) else RedDiscount.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Database Status", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
                Text(
                  text = if (isConfigured) "Connected" else "Not Configured",
                  color = if (isConfigured) GreenStock else RedDiscount,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            }

            // Realtime Badge
            val (rtText, rtColor) = when (realtimeState) {
              is RealtimeConnectionState.Connected -> "Live Connected" to GreenStock
              is RealtimeConnectionState.Connecting -> "Connecting..." to GoldPrimary
              is RealtimeConnectionState.Error -> "Offline / Reconnecting" to RedDiscount
              RealtimeConnectionState.Disconnected -> "Disconnected" to TextSecondaryDark
            }

            Surface(
              color = rtColor.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Supabase Realtime", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
                Text(
                  text = rtText,
                  color = rtColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            }
          }

          Text(
            text = "Last Synced: $formattedTime",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark
          )

          // Sync Action Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { viewModel.refreshFromSupabase(force = true) },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = BlackMain
              ),
              shape = RoundedCornerShape(8.dp),
              enabled = syncStatus !is SupabaseSyncStatus.Syncing
            ) {
              if (syncStatus is SupabaseSyncStatus.Syncing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = BlackMain,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Syncing...", fontWeight = FontWeight.Bold)
              } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Live Data Now", fontWeight = FontWeight.Bold)
              }
            }

            OutlinedButton(
              onClick = { showEditDialog = true },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Config")
            }
          }
        }
      }
    }

    // 2. Status message / error alert
    if (syncStatus is SupabaseSyncStatus.Error) {
      item {
        Surface(
          color = Color(0xFF331515),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RedDiscount.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RedDiscount)
            Text(
              text = (syncStatus as SupabaseSyncStatus.Error).message,
              color = Color(0xFFFFB4AB),
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }
    }

    if (syncStatus is SupabaseSyncStatus.Success) {
      item {
        Surface(
          color = Color(0xFF132B1B),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GreenStock.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenStock)
            Text(
              text = "Successfully synchronized ${(syncStatus as SupabaseSyncStatus.Success).productCount} products with Supabase!",
              color = Color(0xFF8CF49C),
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 3. Schema & Table Discovery Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Database Tables & Schema Discovery",
            style = MaterialTheme.typography.titleSmall,
            color = GoldPrimary,
            fontWeight = FontWeight.Bold
          )

          val schemaInfo = client?.schemaInfo
          val productsTable = schemaInfo?.detectedProductsTable?.ifBlank { "products" } ?: "products"
          val categoriesTable = schemaInfo?.detectedCategoriesTable?.ifBlank { "categories" } ?: "categories"

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Products Table:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryDark)
            Text(productsTable, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Categories Table:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryDark)
            Text(categoriesTable, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Images Storage:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryDark)
            Text(config?.getStorageBucket() ?: "products", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
          }

          HorizontalDivider(color = BlackCardBorder)

          Text(
            text = "Single Source of Truth Principles:",
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimaryDark,
            fontWeight = FontWeight.Bold
          )

          listOf(
            "Products are managed on your website backend and automatically pulled into the app.",
            "Changes in price, images, description, category, sizes, variants, and stock stream live via Supabase Realtime.",
            "Unpublished or deleted products are instantly removed from the active catalog.",
            "Offline resilience: Local Room database mirrors your Supabase data for instant load times and offline browsing.",
            "Security: Only anonymous / publishable keys with Row Level Security (RLS) are used. Admin keys are never bundled."
          ).forEach { note ->
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text("•", color = GoldPrimary, fontWeight = FontWeight.Bold)
              Text(note, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondaryDark)
            }
          }
        }
      }
    }

    // 4. Supabase Edge Function: resend-email System
    item {
      var isSendingTestEmail by remember { mutableStateOf(false) }
      var testEmailMessage by remember { mutableStateOf<String?>(null) }
      var testEmailSuccess by remember { mutableStateOf<Boolean?>(null) }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Resend Email Notification System",
                style = MaterialTheme.typography.titleSmall,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Supabase Edge Function: resend-email",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
              )
            }
            Surface(
              color = Color(0xFF132B1B),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, GreenStock.copy(alpha = 0.5f))
            ) {
              Text(
                text = "ACTIVE",
                color = GreenStock,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          HorizontalDivider(color = BlackCardBorder)

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Function URL:", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
              Text(
                "resend-email",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Admin Recipient:", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
              Text(
                "info.arshadcollection@gmail.com",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF90CAF9),
                fontWeight = FontWeight.Medium
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Customer Recipient:", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
              Text(
                "Customer Checkout Email",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Security / RESEND_API_KEY:", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
              Text(
                "Protected on Server (No Frontend Leak)",
                style = MaterialTheme.typography.bodySmall,
                color = GreenStock,
                fontWeight = FontWeight.Medium
              )
            }
          }

          if (testEmailMessage != null) {
            Surface(
              color = if (testEmailSuccess == true) Color(0xFF132B1B) else Color(0xFF2D1616),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = testEmailMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = if (testEmailSuccess == true) Color(0xFF8CF49C) else Color(0xFFFFB4AB),
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          Button(
            onClick = {
              isSendingTestEmail = true
              testEmailMessage = null
              viewModel.testSupabaseEmail { success, msg ->
                isSendingTestEmail = false
                testEmailSuccess = success
                testEmailMessage = msg
              }
            },
            enabled = !isSendingTestEmail,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
              containerColor = BlackSurfaceVariant,
              contentColor = GoldPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
          ) {
            if (isSendingTestEmail) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = GoldPrimary,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Dispatching Test via Edge Function...", color = GoldPrimary)
            } else {
              Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Send Test Email via Edge Function", color = GoldPrimary, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }

  // Edit Config Dialog
  if (showEditDialog && config != null) {
    var urlInput by remember { mutableStateOf(config.getSupabaseUrl()) }
    var keyInput by remember { mutableStateOf(config.getAnonKey()) }
    var bucketInput by remember { mutableStateOf(config.getStorageBucket()) }
    var isKeyVisible by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      containerColor = BlackSurface,
      title = {
        Text("Configure Supabase Backend", color = GoldPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Enter your project details from the Supabase dashboard (Project Settings -> API):",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark
          )

          OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Project URL (e.g. https://xxx.supabase.co)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = keyInput,
            onValueChange = { keyInput = it },
            label = { Text("Anon / Public Key") },
            singleLine = true,
            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                Icon(
                  imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = null,
                  tint = TextSecondaryDark
                )
              }
            },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = bucketInput,
            onValueChange = { bucketInput = it },
            label = { Text("Storage Bucket (default: products)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Text(
            text = "Note: Do not enter service_role keys. Use the anon/public key with proper RLS policies.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = RedDiscount.copy(alpha = 0.8f)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateSupabaseConfig(urlInput.trim(), keyInput.trim(), bucketInput.trim().ifBlank { "products" })
            showEditDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)
        ) {
          Text("Save & Sync", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) {
          Text("Cancel", color = TextSecondaryDark)
        }
      }
    )
  }
}
