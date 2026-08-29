package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun HomeScreen(
  viewModel: ShopViewModel,
  onProductClick: (Product) -> Unit,
  onCategoryClick: (Category) -> Unit,
  onViewAllClick: (String) -> Unit
) {
  val allProducts by viewModel.allProducts.collectAsState()
  val categories by viewModel.categories.collectAsState()
  val banners by viewModel.banners.collectAsState()
  val flashSaleProducts by viewModel.flashSaleProducts.collectAsState()
  val featuredProducts by viewModel.featuredProducts.collectAsState()
  val bestSellers by viewModel.bestSellers.collectAsState()
  val newArrivals by viewModel.newArrivals.collectAsState()
  val wishlistItems by viewModel.wishlistItems.collectAsState()
  val flashSaleSeconds by viewModel.flashSaleSecondsLeft.collectAsState()

  val wishlistedIds = remember(wishlistItems) { wishlistItems.map { it.product.id }.toSet() }

  // Format flash sale countdown
  val hours = flashSaleSeconds / 3600
  val minutes = (flashSaleSeconds % 3600) / 60
  val seconds = flashSaleSeconds % 60

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // 1. Search Bar shortcut strip
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(BlackSurfaceVariant)
          .border(1.dp, BlackCardBorder, RoundedCornerShape(12.dp))
          .clickable {
            viewModel.selectedTab.value = 2 // Switch to Search tab
          }
          .padding(horizontal = 14.dp, vertical = 12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = GoldPrimary
          )
          Text(
            text = "Search Shalwar Kameez, Bedsheets, Crockery...",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }

    // 2. Promotional Hero Banner Carousel
    item {
      val context = LocalContext.current
      val activeBanner = banners.firstOrNull()
      val bannerResId = remember(activeBanner) {
        val res = activeBanner?.imageResName ?: "hero_banner_luxury_1787981532790"
        context.resources.getIdentifier(res, "drawable", context.packageName)
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .height(180.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      ) {
        if (bannerResId != 0) {
          Image(
            painter = painterResource(id = bannerResId),
            contentDescription = "Promo Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.horizontalGradient(
                listOf(
                  BlackMain.copy(alpha = 0.92f),
                  BlackMain.copy(alpha = 0.65f),
                  Color.Transparent
                )
              )
            )
        )

        // Banner content
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
          verticalArrangement = Arrangement.SpaceBetween,
          horizontalAlignment = Alignment.Start
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
              color = GoldPrimary,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "FESTIVE SALE",
                color = BlackMain,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }

            Text(
              text = activeBanner?.title ?: "Festive Luxury Collection",
              style = MaterialTheme.typography.titleLarge,
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            Text(
              text = activeBanner?.subtitle ?: "Style • Quality • Trust | Up to 25% Off",
              style = MaterialTheme.typography.bodySmall,
              color = GoldLight,
              maxLines = 2
            )
          }

          Button(
            onClick = {
              val target = activeBanner?.targetCategory ?: "men"
              val cat = categories.find { it.id == target }
              if (cat != null) onCategoryClick(cat) else onViewAllClick("All Products")
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = BlackMain
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Text(
              text = activeBanner?.buttonText ?: "Shop Now",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }

    // 3. Category Horizontal Row
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Text(
            text = "View All",
            style = MaterialTheme.typography.labelMedium,
            color = GoldPrimary,
            modifier = Modifier.clickable { viewModel.selectedTab.value = 1 }
          )
        }

        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(categories) { category ->
            CategoryChip(
              category = category,
              isSelected = false,
              onClick = { onCategoryClick(category) }
            )
          }
        }
      }
    }

    // 4. Flash Sale with Live Countdown Timer
    if (flashSaleProducts.isNotEmpty()) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(BlackSurfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = GoldWarm,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "Flash Sale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )

              // Timer badges
              Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                TimerBox(String.format("%02d", hours))
                Text(":", color = GoldPrimary, fontWeight = FontWeight.Bold)
                TimerBox(String.format("%02d", minutes))
                Text(":", color = GoldPrimary, fontWeight = FontWeight.Bold)
                TimerBox(String.format("%02d", seconds))
              }
            }

            Text(
              text = "View All",
              style = MaterialTheme.typography.labelMedium,
              color = GoldPrimary,
              modifier = Modifier.clickable { onViewAllClick("Flash Sale") }
            )
          }

          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            items(flashSaleProducts) { product ->
              ProductCard(
                product = product,
                isWishlisted = wishlistedIds.contains(product.id),
                onProductClick = { onProductClick(product) },
                onWishlistToggle = { viewModel.toggleWishlist(product.id) },
                onAddToCart = { viewModel.addToCart(product) },
                modifier = Modifier.width(180.dp)
              )
            }
          }
        }
      }
    }

    // 5. Featured Products
    item {
      ProductSectionRow(
        title = "Featured Products",
        subtitle = "Handpicked premium essentials",
        products = featuredProducts,
        wishlistedIds = wishlistedIds,
        onProductClick = onProductClick,
        onWishlistToggle = { viewModel.toggleWishlist(it) },
        onAddToCart = { viewModel.addToCart(it) },
        onViewAllClick = { onViewAllClick("Featured Products") }
      )
    }

    // 6. New Arrivals
    item {
      ProductSectionRow(
        title = "New Arrivals",
        subtitle = "Latest additions to Arshad Collection",
        products = newArrivals,
        wishlistedIds = wishlistedIds,
        onProductClick = onProductClick,
        onWishlistToggle = { viewModel.toggleWishlist(it) },
        onAddToCart = { viewModel.addToCart(it) },
        onViewAllClick = { onViewAllClick("New Arrivals") }
      )
    }

    // 7. Best Sellers
    item {
      ProductSectionRow(
        title = "Best Sellers",
        subtitle = "Customer favorites across Pakistan",
        products = bestSellers,
        wishlistedIds = wishlistedIds,
        onProductClick = onProductClick,
        onWishlistToggle = { viewModel.toggleWishlist(it) },
        onAddToCart = { viewModel.addToCart(it) },
        onViewAllClick = { onViewAllClick("Best Sellers") }
      )
    }

    // 8. Men's & Women's Highlights
    item {
      val menProducts = allProducts.filter { it.categoryId == "men" }
      if (menProducts.isNotEmpty()) {
        ProductSectionRow(
          title = "Men's Collection",
          subtitle = "Traditional & formal luxury wear",
          products = menProducts,
          wishlistedIds = wishlistedIds,
          onProductClick = onProductClick,
          onWishlistToggle = { viewModel.toggleWishlist(it) },
          onAddToCart = { viewModel.addToCart(it) },
          onViewAllClick = { onViewAllClick("Men's Collection") }
        )
      }
    }

    // 9. Home & Living & Cultural Highlights
    item {
      val homeCultural = allProducts.filter { it.categoryId in listOf("home_textile", "crockery", "cultural") }
      if (homeCultural.isNotEmpty()) {
        ProductSectionRow(
          title = "Home, Textile & Cultural Heritage",
          subtitle = "Bedsheets, curtains, crockery & Multani pottery",
          products = homeCultural,
          wishlistedIds = wishlistedIds,
          onProductClick = onProductClick,
          onWishlistToggle = { viewModel.toggleWishlist(it) },
          onAddToCart = { viewModel.addToCart(it) },
          onViewAllClick = { onViewAllClick("Home & Living") }
        )
      }
    }

    // 10. Nationwide Trust Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            ArshadBrandLogo(size = 32)
            Column {
              Text(
                text = "Arshad Collection Guarantee",
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                fontSize = 14.sp
              )
              Text(
                text = "Style • Quality • Trust",
                color = TextSecondaryDark,
                fontSize = 11.sp
              )
            }
          }

          Divider(color = BlackDivider)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            TrustItem(icon = Icons.Outlined.LocalShipping, label = "Cash on\nDelivery")
            TrustItem(icon = Icons.Outlined.Verified, label = "100% Quality\nAssured")
            TrustItem(icon = Icons.Outlined.SupportAgent, label = "WhatsApp\nSupport")
            TrustItem(icon = Icons.Outlined.Autorenew, label = "Easy Return\nPolicy")
          }
        }
      }
    }
  }
}

@Composable
fun ProductSectionRow(
  title: String,
  subtitle: String,
  products: List<Product>,
  wishlistedIds: Set<Long>,
  onProductClick: (Product) -> Unit,
  onWishlistToggle: (Long) -> Unit,
  onAddToCart: (Product) -> Unit,
  onViewAllClick: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondaryDark
        )
      }

      Text(
        text = "View All",
        style = MaterialTheme.typography.labelMedium,
        color = GoldPrimary,
        modifier = Modifier.clickable { onViewAllClick() }
      )
    }

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(products) { product ->
        ProductCard(
          product = product,
          isWishlisted = wishlistedIds.contains(product.id),
          onProductClick = { onProductClick(product) },
          onWishlistToggle = { onWishlistToggle(product.id) },
          onAddToCart = { onAddToCart(product) },
          modifier = Modifier.width(180.dp)
        )
      }
    }
  }
}

@Composable
fun TimerBox(text: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(GoldPrimary)
      .padding(horizontal = 5.dp, vertical = 2.dp)
  ) {
    Text(
      text = text,
      color = BlackMain,
      fontSize = 11.sp,
      fontWeight = FontWeight.ExtraBold
    )
  }
}

@Composable
fun TrustItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = GoldPrimary,
      modifier = Modifier.size(20.dp)
    )
    Text(
      text = label,
      fontSize = 10.sp,
      color = TextSecondaryDark,
      textAlign = TextAlign.Center,
      lineHeight = 13.sp
    )
  }
}
