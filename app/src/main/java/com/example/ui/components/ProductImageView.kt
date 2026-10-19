package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.ui.theme.BlackCardBorder
import com.example.ui.theme.BlackMain
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.BlackSurfaceVariant
import com.example.ui.theme.GoldPrimary

@Composable
fun ProductImageView(
  imageSource: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop
) {
  val context = LocalContext.current
  val trimmed = imageSource.trim()

  val isExternalSource = trimmed.startsWith("http://") ||
                         trimmed.startsWith("https://") ||
                         trimmed.startsWith("file://") ||
                         trimmed.startsWith("content://") ||
                         trimmed.startsWith("/")

  if (isExternalSource) {
    SubcomposeAsyncImage(
      model = trimmed,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
      loading = {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(BlackSurfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = GoldPrimary,
            strokeWidth = 2.dp
          )
        }
      },
      error = {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(BlackSurfaceVariant, BlackMain)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            tint = GoldPrimary.copy(alpha = 0.4f),
            modifier = Modifier.size(36.dp)
          )
        }
      }
    )
  } else {
    val resId = remember(trimmed) {
      if (trimmed.isNotBlank()) {
        context.resources.getIdentifier(trimmed, "drawable", context.packageName)
      } else 0
    }

    if (resId != 0) {
      Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
      )
    } else {
      Box(
        modifier = modifier
          .background(
            Brush.verticalGradient(
              listOf(BlackSurfaceVariant, BlackMain)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ShoppingBag,
          contentDescription = null,
          tint = GoldPrimary.copy(alpha = 0.4f),
          modifier = Modifier.size(36.dp)
        )
      }
    }
  }
}
