package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.supabase.RealtimeConnectionState
import com.example.data.supabase.SupabaseSyncStatus
import com.example.ui.theme.*

@Composable
fun SupabaseSyncBanner(
  syncStatus: SupabaseSyncStatus,
  realtimeState: RealtimeConnectionState,
  isConfigured: Boolean,
  onRefreshClick: () -> Unit,
  onConfigureClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = syncStatus is SupabaseSyncStatus.Syncing ||
      syncStatus is SupabaseSyncStatus.Error ||
      !isConfigured,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically(),
    modifier = modifier
  ) {
    when {
      !isConfigured -> {
        Surface(
          color = BlackSurfaceVariant,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(18.dp)
              )
              Column {
                Text(
                  text = "Offline Catalog Mode",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimaryDark
                )
                Text(
                  text = "Connect Supabase in Admin to enable live website sync.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = TextSecondaryDark
                )
              }
            }

            if (onConfigureClick != null) {
              TextButton(
                onClick = onConfigureClick,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Setup",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = GoldPrimary
                )
              }
            }
          }
        }
      }

      syncStatus is SupabaseSyncStatus.Syncing -> {
        Surface(
          color = BlackSurfaceVariant,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = GoldPrimary,
              strokeWidth = 2.dp
            )
            Text(
              text = syncStatus.message,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = GoldPrimary,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      syncStatus is SupabaseSyncStatus.Error -> {
        Surface(
          color = Color(0xFF2A1414),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, RedDiscount.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = RedDiscount,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Sync error: ${syncStatus.message}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFFFFB4AB),
                maxLines = 1
              )
            }

            Button(
              onClick = onRefreshClick,
              colors = ButtonDefaults.buttonColors(
                containerColor = RedDiscount.copy(alpha = 0.8f)
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Text(
                text = "Retry",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun SupabaseStatusChip(
  syncStatus: SupabaseSyncStatus,
  realtimeState: RealtimeConnectionState,
  isConfigured: Boolean,
  onRefreshClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(BlackSurfaceVariant)
      .border(0.5.dp, BlackCardBorder, RoundedCornerShape(20.dp))
      .clickable { onRefreshClick() }
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    when {
      !isConfigured -> {
        Icon(
          imageVector = Icons.Default.CloudOff,
          contentDescription = null,
          tint = TextSecondaryDark,
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = "Offline Mode",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = TextSecondaryDark
        )
      }
      syncStatus is SupabaseSyncStatus.Syncing -> {
        CircularProgressIndicator(
          modifier = Modifier.size(10.dp),
          color = GoldPrimary,
          strokeWidth = 1.5.dp
        )
        Text(
          text = "Syncing...",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = GoldPrimary
        )
      }
      realtimeState is RealtimeConnectionState.Connected -> {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(GreenStock)
        )
        Text(
          text = "Supabase Live",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
          color = GreenStock
        )
      }
      else -> {
        Icon(
          imageVector = Icons.Default.CloudDone,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = "Supabase Synced",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = GoldPrimary
        )
      }
    }

    Icon(
      imageVector = Icons.Default.Refresh,
      contentDescription = "Refresh",
      tint = TextSecondaryDark,
      modifier = Modifier.size(12.dp)
    )
  }
}
