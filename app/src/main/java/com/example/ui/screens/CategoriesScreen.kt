package com.example.ui.screens

import androidx.compose.foundation.Image
import coil.compose.SubcomposeAsyncImage
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.components.CategoryCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
import com.example.ui.components.ProductImageView
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun CategoriesScreen(
  viewModel: ShopViewModel,
  onProductClick: (Product) -> Unit
) {
  val categories by viewModel.categories.collectAsState()
  val allProducts by viewModel.allProducts.collectAsState()
  val wishlistItems by viewModel.wishlistItems.collectAsState()
  val wishlistedIds = remember(wishlistItems) { wishlistItems.map { it.product.id }.toSet() }

  var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull()) }
  var selectedSubcategory by remember { mutableStateOf<String?>(null) }

  val context = LocalContext.current

  // Filter products by selected category and optional subcategory
  val displayedProducts = remember(selectedCategory, selectedSubcategory, allProducts) {
    if (selectedCategory == null) {
      allProducts
    } else {
      allProducts.filter { product ->
        val catMatches = product.categoryId == selectedCategory?.id
        val subMatches = selectedSubcategory == null || product.subcategoryId.equals(selectedSubcategory, ignoreCase = true) || product.tags.any { it.equals(selectedSubcategory, ignoreCase = true) }
        catMatches && subMatches
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // 1. Horizontal Category Selector Bar
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .background(BlackSurface)
        .padding(vertical = 12.dp),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // All Categories Chip
      item {
        val isAllSelected = selectedCategory == null
        Surface(
          onClick = {
            selectedCategory = null
            selectedSubcategory = null
          },
          shape = RoundedCornerShape(20.dp),
          color = if (isAllSelected) GoldPrimary else BlackSurfaceVariant,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAllSelected) GoldPrimary else BlackCardBorder
          )
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Category,
              contentDescription = null,
              tint = if (isAllSelected) BlackMain else GoldPrimary,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "All Categories",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isAllSelected) BlackMain else Color.White,
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }

      items(categories) { category ->
        val isSelected = selectedCategory?.id == category.id

        Surface(
          onClick = {
            selectedCategory = category
            selectedSubcategory = null
          },
          shape = RoundedCornerShape(20.dp),
          color = if (isSelected) GoldPrimary else BlackSurfaceVariant,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) GoldPrimary else BlackCardBorder
          )
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(BlackSurface),
              contentAlignment = Alignment.Center
            ) {
              ProductImageView(
                imageSource = category.imageResName,
                contentDescription = category.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }

            Text(
              text = category.name,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) BlackMain else Color.White,
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }
    }

    // 2. Main Content: Either Category Cards Grid (when All is selected) or Products in Selected Category
    if (selectedCategory == null) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Browse Collections (${categories.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Style • Quality • Trust",
            style = MaterialTheme.typography.labelSmall,
            color = GoldPrimary
          )
        }

        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 90.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(categories) { category ->
            CategoryCard(
              category = category,
              onClick = {
                selectedCategory = category
                selectedSubcategory = null
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    } else {
      // Subcategories Pills (if category has subcategories)
      val subcategories = selectedCategory?.subcategories ?: emptyList()
      if (subcategories.isNotEmpty()) {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .background(BlackSurfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 8.dp),
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            FilterChip(
              selected = selectedSubcategory == null,
              onClick = { selectedSubcategory = null },
              label = { Text("All ${selectedCategory?.name ?: ""}") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                selectedLabelColor = GoldPrimary,
                containerColor = BlackSurface,
                labelColor = TextSecondaryDark
              )
            )
          }

          items(subcategories) { subcat ->
            val isSelected = selectedSubcategory == subcat
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedSubcategory = if (isSelected) null else subcat
              },
              label = { Text(subcat) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                selectedLabelColor = GoldPrimary,
                containerColor = BlackSurface,
                labelColor = TextSecondaryDark
              )
            )
          }
        }
      }

      // Category Header Summary
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "${selectedCategory?.name ?: "Products"} (${displayedProducts.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Text(
          text = "All Collections",
          style = MaterialTheme.typography.labelSmall,
          color = GoldPrimary,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.clickable {
            selectedCategory = null
            selectedSubcategory = null
          }
        )
      }

      // Products Grid
      if (displayedProducts.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.Category,
          title = "No Products in this Category",
          subtitle = "We are constantly adding new items to Arshad Collection.",
          actionText = "Explore All Categories",
          onActionClick = {
            selectedCategory = null
            selectedSubcategory = null
          }
        )
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(displayedProducts) { product ->
            ProductCard(
              product = product,
              isWishlisted = wishlistedIds.contains(product.id),
              onProductClick = { onProductClick(product) },
              onWishlistToggle = { viewModel.toggleWishlist(product.id) },
              onAddToCart = { viewModel.addToCart(product) },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}
