package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.ArshadBrandLogo
import com.example.ui.components.ArshadTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
  viewModel: ShopViewModel,
  onBackClick: () -> Unit,
  onCartClick: () -> Unit,
  onBuyNowClick: () -> Unit
) {
  val product = viewModel.selectedProduct.collectAsState().value
  val selectedSize by viewModel.selectedSize.collectAsState()
  val selectedColor by viewModel.selectedColor.collectAsState()
  val selectedQuantity by viewModel.selectedQuantity.collectAsState()
  val reviews by viewModel.productReviews.collectAsState()
  val wishlistItems by viewModel.wishlistItems.collectAsState()
  val cartItems by viewModel.cartItems.collectAsState()

  val context = LocalContext.current

  if (product == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(BlackMain),
      contentAlignment = Alignment.Center
    ) {
      Text("Product not found", color = Color.White)
    }
    return
  }

  val isWishlisted = remember(wishlistItems, product) {
    wishlistItems.any { it.product.id == product.id }
  }

  var activeImageIndex by remember { mutableStateOf(0) }
  var showWriteReviewDialog by remember { mutableStateOf(false) }
  var addedToCartSnackbar by remember { mutableStateOf(false) }

  // Review Dialog form state
  var reviewerName by remember { mutableStateOf("") }
  var reviewRating by remember { mutableStateOf(5f) }
  var reviewText by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      ArshadTopBar(
        title = product.name,
        showBackButton = true,
        onBackClick = onBackClick,
        wishlistCount = wishlistItems.size,
        cartCount = cartItems.sumOf { it.quantity },
        onWishlistClick = { viewModel.selectedTab.value = 4 },
        onCartClick = onCartClick
      )
    },
    bottomBar = {
      // Bottom Action Bar: WhatsApp Inquiry + Add to Cart + Buy Now
      Surface(
        color = BlackSurface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // WhatsApp Inquiry
          IconButton(
            onClick = {
              val msg = WhatsAppHelper.createProductInquiryMessage(
                productName = product.name,
                sku = product.sku,
                size = selectedSize,
                color = selectedColor,
                quantity = selectedQuantity,
                price = product.currentPrice * selectedQuantity
              )
              WhatsAppHelper.openWhatsApp(context, msg)
            },
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(WhatsAppGreen.copy(alpha = 0.15f))
              .border(1.dp, WhatsAppGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
          ) {
            Icon(
              imageVector = Icons.Default.Chat,
              contentDescription = "WhatsApp Inquiry",
              tint = WhatsAppGreen
            )
          }

          // Add to Cart
          Button(
            onClick = {
              viewModel.addToCart(product, selectedSize, selectedColor, selectedQuantity)
              addedToCartSnackbar = true
            },
            enabled = product.isInStock,
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = BlackSurfaceVariant,
              contentColor = GoldPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
          ) {
            Icon(
              imageVector = Icons.Outlined.AddShoppingCart,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Add to Cart",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          // Buy Now
          Button(
            onClick = {
              viewModel.addToCart(product, selectedSize, selectedColor, selectedQuantity)
              onBuyNowClick()
            },
            enabled = product.isInStock,
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = BlackMain
            )
          ) {
            Text(
              text = "Buy Now",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
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
      contentPadding = PaddingValues(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Added to Cart Toast Banner
      if (addedToCartSnackbar) {
        item {
          Surface(
            color = GreenStock.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GreenStock),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenStock, modifier = Modifier.size(16.dp))
                Text("Item added to cart!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
              Text(
                text = "View Cart",
                color = GoldPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onCartClick() }
              )
            }
          }
        }
      }

      // 2. Large Image Showcase
      item {
        val currentImageName = product.images.getOrNull(activeImageIndex) ?: product.images.firstOrNull() ?: ""
        val imageResId = remember(currentImageName) {
          if (currentImageName.isNotBlank()) {
            context.resources.getIdentifier(currentImageName, "drawable", context.packageName)
          } else 0
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
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
                .background(BlackSurfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = GoldPrimary.copy(alpha = 0.4f),
                modifier = Modifier.size(72.dp)
              )
            }
          }

          // Discount Tag
          if (product.discountPercent > 0) {
            Surface(
              color = RedDiscount,
              shape = RoundedCornerShape(bottomEnd = 12.dp),
              modifier = Modifier.align(Alignment.TopStart)
            ) {
              Text(
                text = "SAVE ${product.discountPercent}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }

          // Wishlist Heart button
          IconButton(
            onClick = { viewModel.toggleWishlist(product.id) },
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(12.dp)
              .size(40.dp)
              .background(Color.Black.copy(alpha = 0.6f), CircleShape)
          ) {
            Icon(
              imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Wishlist",
              tint = if (isWishlisted) RedDiscount else Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }

      // 3. Thumbnails Row (if multiple images)
      if (product.images.size > 1) {
        item {
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(product.images.indices.toList()) { index ->
              val thumbName = product.images[index]
              val thumbResId = remember(thumbName) {
                context.resources.getIdentifier(thumbName, "drawable", context.packageName)
              }

              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .border(
                    width = if (activeImageIndex == index) 2.dp else 1.dp,
                    color = if (activeImageIndex == index) GoldPrimary else BlackCardBorder,
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable { activeImageIndex = index }
              ) {
                if (thumbResId != 0) {
                  Image(
                    painter = painterResource(id = thumbResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                }
              }
            }
          }
        }
      }

      // 4. Product Title, Brand, SKU, Price & Stock
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = product.brand.uppercase(),
              style = MaterialTheme.typography.labelMedium,
              color = GoldPrimary,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )

            Text(
              text = "SKU: ${product.sku}",
              style = MaterialTheme.typography.labelSmall,
              color = TextMutedDark
            )
          }

          Text(
            text = product.name,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          // Rating Row
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldWarm, modifier = Modifier.size(16.dp))
            Text("${product.rating}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("(${product.reviewCount} customer reviews)", color = TextSecondaryDark, fontSize = 13.sp)
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Price and Stock
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = CurrencyFormatter.format(product.currentPrice),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = GoldPrimary
              )

              if (product.salePrice != null && product.salePrice < product.price) {
                Text(
                  text = CurrencyFormatter.format(product.price),
                  style = MaterialTheme.typography.titleMedium,
                  textDecoration = TextDecoration.LineThrough,
                  color = TextMutedDark
                )
              }
            }

            // Stock Badge
            if (product.stock > 0) {
              Surface(
                color = if (product.isLowStock) OrangeWarning.copy(alpha = 0.15f) else GreenStock.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (product.isLowStock) OrangeWarning else GreenStock)
              ) {
                Text(
                  text = if (product.isLowStock) "Only ${product.stock} Left" else "In Stock (${product.stock})",
                  color = if (product.isLowStock) OrangeWarning else GreenStock,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            } else {
              Surface(
                color = RedDiscount.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RedDiscount)
              ) {
                Text(
                  text = "Out of Stock",
                  color = RedDiscount,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }

      // 5. Size Selection
      if (product.sizes.isNotEmpty()) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Select Size / Variant", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
              Text("Size Guide", color = GoldPrimary, fontSize = 12.sp, modifier = Modifier.clickable { })
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(product.sizes) { size ->
                val isSelected = selectedSize == size
                Surface(
                  onClick = { viewModel.selectedSize.value = size },
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) GoldPrimary else BlackSurfaceVariant,
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldPrimary else BlackCardBorder),
                  modifier = Modifier.height(40.dp)
                ) {
                  Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = size,
                      color = if (isSelected) BlackMain else Color.White,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 13.sp
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 6. Color Selection
      if (product.colors.isNotEmpty()) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("Select Color: $selectedColor", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(product.colors) { color ->
                val isSelected = selectedColor == color
                Surface(
                  onClick = { viewModel.selectedColor.value = color },
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) GoldPrimary else BlackSurfaceVariant,
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldPrimary else BlackCardBorder),
                  modifier = Modifier.height(38.dp)
                ) {
                  Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = color,
                      color = if (isSelected) BlackMain else Color.White,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 13.sp
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 7. Quantity Selector
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Quantity", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(BlackSurfaceVariant)
              .border(1.dp, BlackCardBorder, RoundedCornerShape(8.dp))
          ) {
            IconButton(
              onClick = {
                if (selectedQuantity > 1) viewModel.selectedQuantity.value--
              },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Text(
              text = "$selectedQuantity",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp)
            )

            IconButton(
              onClick = {
                if (selectedQuantity < product.stock) viewModel.selectedQuantity.value++
              },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      // 8. Description & Highlights Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          colors = CardDefaults.cardColors(containerColor = BlackCard),
          border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("Product Overview", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
              text = product.description,
              color = TextSecondaryDark,
              style = MaterialTheme.typography.bodyMedium,
              lineHeight = 22.sp
            )
          }
        }
      }

      // 9. Delivery & Return Policy in Pakistan Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          colors = CardDefaults.cardColors(containerColor = BlackCard),
          border = androidx.compose.foundation.BorderStroke(1.dp, BlackCardBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(imageVector = Icons.Outlined.LocalShipping, contentDescription = null, tint = GoldPrimary)
              Text("Delivery & Returns Across Pakistan", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            Divider(color = BlackDivider)

            Text("• Cash on Delivery (COD) available nationwide", color = TextSecondaryDark, fontSize = 13.sp)
            Text("• Delivery within 2 to 4 business days via TCS / Leopard", color = TextSecondaryDark, fontSize = 13.sp)
            Text("• Free Delivery on orders above Rs. 3,000", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("• 7-day hassle-free exchange & return policy", color = TextSecondaryDark, fontSize = 13.sp)
          }
        }
      }

      // 10. Customer Reviews Section
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Customer Reviews (${reviews.size})", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            Button(
              onClick = { showWriteReviewDialog = true },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary.copy(alpha = 0.15f),
                contentColor = GoldPrimary
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Text("Write Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          if (reviews.isEmpty()) {
            Text("No customer reviews yet. Be the first to leave a review!", color = TextMutedDark, fontSize = 13.sp)
          } else {
            reviews.forEach { rev ->
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
                      Text(rev.customerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                      if (rev.isVerifiedPurchase) {
                        Surface(
                          color = GreenStock.copy(alpha = 0.2f),
                          shape = RoundedCornerShape(4.dp)
                        ) {
                          Text("Verified Buyer", color = GreenStock, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                      }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                      repeat(rev.rating.toInt()) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldWarm, modifier = Modifier.size(12.dp))
                      }
                    }
                  }

                  Text(rev.reviewText, color = TextSecondaryDark, fontSize = 13.sp, lineHeight = 18.sp)
                }
              }
            }
          }
        }
      }
    }
  }

  // Write Review Dialog
  if (showWriteReviewDialog) {
    AlertDialog(
      onDismissRequest = { showWriteReviewDialog = false },
      containerColor = BlackSurface,
      title = {
        Text("Write a Review", color = GoldPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = reviewerName,
            onValueChange = { reviewerName = it },
            label = { Text("Your Name (e.g. Tariq from Lahore)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("Rating:", color = Color.White)
            (1..5).forEach { star ->
              IconButton(onClick = { reviewRating = star.toFloat() }, modifier = Modifier.size(32.dp)) {
                Icon(
                  imageVector = if (star <= reviewRating) Icons.Default.Star else Icons.Outlined.StarBorder,
                  contentDescription = null,
                  tint = GoldWarm
                )
              }
            }
          }

          OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Your Feedback & Experience") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (reviewText.isNotBlank()) {
              viewModel.submitReview(product.id, reviewerName, reviewRating, reviewText)
              showWriteReviewDialog = false
              reviewerName = ""
              reviewText = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackMain)
        ) {
          Text("Submit Review", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showWriteReviewDialog = false }) {
          Text("Cancel", color = TextSecondaryDark)
        }
      }
    )
  }
}
