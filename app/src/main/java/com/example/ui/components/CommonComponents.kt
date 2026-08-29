package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.ui.theme.*
import com.example.util.CurrencyFormatter

@Composable
fun ArshadBrandLogo(
  modifier: Modifier = Modifier,
  size: Int = 40
) {
  val context = LocalContext.current
  val logoResId = remember(context) {
    var id = context.resources.getIdentifier("logo_arshad_official", "drawable", context.packageName)
    if (id == 0) {
      id = context.resources.getIdentifier("arshad_official_logo_1787990063785", "drawable", context.packageName)
    }
    id
  }

  Box(
    modifier = modifier
      .size(size.dp)
      .aspectRatio(1f)
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    if (logoResId != 0) {
      Image(
        painter = painterResource(id = logoResId),
        contentDescription = "Official Arshad Collection Logo",
        modifier = Modifier
          .fillMaxSize()
          .aspectRatio(1f),
        contentScale = ContentScale.Fit
      )
    } else {
      Text(
        text = "AC",
        color = GoldPrimary,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        fontSize = (size / 2.2).sp
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArshadTopBar(
  title: String? = null,
  showBackButton: Boolean = false,
  onBackClick: () -> Unit = {},
  wishlistCount: Int = 0,
  cartCount: Int = 0,
  notificationCount: Int = 0,
  onSearchClick: () -> Unit = {},
  onWishlistClick: () -> Unit = {},
  onCartClick: () -> Unit = {},
  onNotificationClick: () -> Unit = {}
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    tonalElevation = 4.dp,
    shadowElevation = 2.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (showBackButton) {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Back",
              tint = GoldPrimary
            )
          }
        } else {
          ArshadBrandLogo(size = 38)
        }

        Column {
          Text(
            text = title ?: "ARSHAD COLLECTION",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (title == null) {
            Text(
              text = "STYLE • QUALITY • TRUST",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            )
          }
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        IconButton(
          onClick = onSearchClick,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        IconButton(
          onClick = onWishlistClick,
          modifier = Modifier.size(40.dp)
        ) {
          BadgedBox(
            badge = {
              if (wishlistCount > 0) {
                Badge(
                  containerColor = GoldPrimary,
                  contentColor = BlackMain
                ) {
                  Text(text = wishlistCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          ) {
            Icon(
              imageVector = if (wishlistCount > 0) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Wishlist",
              tint = if (wishlistCount > 0) RedDiscount else MaterialTheme.colorScheme.onSurface
            )
          }
        }

        IconButton(
          onClick = onCartClick,
          modifier = Modifier.size(40.dp)
        ) {
          BadgedBox(
            badge = {
              if (cartCount > 0) {
                Badge(
                  containerColor = GoldPrimary,
                  contentColor = BlackMain
                ) {
                  Text(text = cartCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Outlined.ShoppingBag,
              contentDescription = "Cart",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

@Composable
fun ProductCard(
  product: Product,
  isWishlisted: Boolean = false,
  onProductClick: () -> Unit,
  onWishlistToggle: () -> Unit,
  onAddToCart: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val imageResId = remember(product.images) {
    val firstImg = product.images.firstOrNull() ?: ""
    if (firstImg.isNotBlank()) {
      context.resources.getIdentifier(firstImg, "drawable", context.packageName)
    } else 0
  }

  Card(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable { onProductClick() }
      .border(1.dp, BlackCardBorder, RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      // Product Image Container with Badges
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .background(BlackSurface)
      ) {
        if (imageResId != 0) {
          Image(
            painter = painterResource(id = imageResId),
            contentDescription = product.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        } else {
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
              tint = GoldPrimary.copy(alpha = 0.5f),
              modifier = Modifier.size(48.dp)
            )
          }
        }

        // Discount Tag
        if (product.discountPercent > 0) {
          Surface(
            color = RedDiscount,
            shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 10.dp),
            modifier = Modifier.align(Alignment.TopStart)
          ) {
            Text(
              text = "-${product.discountPercent}%",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        // Stock status badge
        if (!product.isInStock) {
          Surface(
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(8.dp)
          ) {
            Text(
              text = "Out of Stock",
              color = RedDiscount,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        } else if (product.isLowStock) {
          Surface(
            color = OrangeWarning.copy(alpha = 0.9f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(8.dp)
          ) {
            Text(
              text = "Only ${product.stock} left",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Wishlist Button
        IconButton(
          onClick = onWishlistToggle,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(34.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
          Icon(
            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Wishlist",
            tint = if (isWishlisted) RedDiscount else Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Product Details
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = product.brand,
          style = MaterialTheme.typography.labelSmall,
          color = GoldPrimary,
          maxLines = 1
        )

        Text(
          text = product.name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          minLines = 2
        )

        // Rating
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = GoldWarm,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "${product.rating}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "(${product.reviewCount})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Price Row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = CurrencyFormatter.format(product.currentPrice),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary
          )

          if (product.salePrice != null && product.salePrice < product.price) {
            Text(
              text = CurrencyFormatter.format(product.price),
              style = MaterialTheme.typography.bodySmall,
              textDecoration = TextDecoration.LineThrough,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Add to Cart button
        Button(
          onClick = onAddToCart,
          enabled = product.isInStock,
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = BlackMain,
            disabledContainerColor = BlackCardBorder,
            disabledContentColor = TextMutedDark
          ),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.AddShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (product.isInStock) "Add to Cart" else "Unavailable",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
fun CategoryChip(
  category: Category,
  isSelected: Boolean = false,
  onClick: () -> Unit
) {
  val context = LocalContext.current
  val imageResId = remember(category.imageResName) {
    if (category.imageResName.isNotBlank()) {
      context.resources.getIdentifier(category.imageResName, "drawable", context.packageName)
    } else 0
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(76.dp)
      .clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(60.dp)
        .clip(CircleShape)
        .background(if (isSelected) GoldPrimary else BlackSurfaceVariant)
        .border(
          width = if (isSelected) 2.dp else 1.dp,
          color = if (isSelected) GoldPrimary else BlackCardBorder,
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      if (imageResId != 0) {
        Image(
          painter = painterResource(id = imageResId),
          contentDescription = category.name,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      } else {
        Icon(
          imageVector = Icons.Default.Category,
          contentDescription = null,
          tint = if (isSelected) BlackMain else GoldPrimary,
          modifier = Modifier.size(26.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = category.name,
      style = MaterialTheme.typography.labelSmall,
      color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun OrderStatusTimeline(
  currentStatus: OrderStatus,
  modifier: Modifier = Modifier
) {
  val steps = listOf(
    OrderStatus.PENDING,
    OrderStatus.CONFIRMED,
    OrderStatus.PROCESSING,
    OrderStatus.PACKED,
    OrderStatus.SHIPPED,
    OrderStatus.OUT_FOR_DELIVERY,
    OrderStatus.DELIVERED
  )

  val activeIndex = currentStatus.stepIndex

  Column(modifier = modifier.fillMaxWidth()) {
    steps.forEachIndexed { index, step ->
      val isCompleted = activeIndex >= step.stepIndex && activeIndex >= 0
      val isCurrent = activeIndex == step.stepIndex
      val isCancelled = currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.RETURNED

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Step Icon Indicator
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
              when {
                isCancelled -> RedDiscount.copy(alpha = 0.2f)
                isCurrent -> GoldPrimary
                isCompleted -> GreenStock
                else -> BlackSurfaceVariant
              }
            )
            .border(
              width = 1.dp,
              color = when {
                isCancelled -> RedDiscount
                isCurrent -> GoldPrimary
                isCompleted -> GreenStock
                else -> BlackCardBorder
              },
              shape = CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          if (isCompleted && !isCurrent) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          } else {
            Text(
              text = "${index + 1}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isCurrent) BlackMain else TextSecondaryDark
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = step.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
            color = when {
              isCurrent -> GoldPrimary
              isCompleted -> MaterialTheme.colorScheme.onSurface
              else -> TextMutedDark
            }
          )
        }

        if (isCurrent) {
          Surface(
            color = GoldPrimary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "Current State",
              color = GoldPrimary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun EmptyStateView(
  icon: ImageVector,
  title: String,
  subtitle: String,
  actionText: String? = null,
  onActionClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(88.dp)
          .clip(CircleShape)
          .background(BlackSurfaceVariant)
          .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      if (actionText != null) {
        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = onActionClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = BlackMain
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
          Text(text = actionText, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
