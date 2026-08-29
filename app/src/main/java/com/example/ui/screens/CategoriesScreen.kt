package com.example.ui.screens

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
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
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
      items(categories) { category ->
        val isSelected = selectedCategory?.id == category.id
        val imageResId = remember(category.imageResName) {
          if (category.imageResName.isNotBlank()) {
            context.resources.getIdentifier(category.imageResName, "drawable", context.packageName)
          } else 0
        }

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
            if (imageResId != 0) {
              Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape),
                contentScale = ContentScale.Crop
              )
            }
            Text(
              text = category.name,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) BlackMain else Color.White
            )
          }
        }
      }
    }

    // 2. Subcategories Pills (if category has subcategories)
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

    // 3. Category Header Summary
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${selectedCategory?.name ?: "All Products"} (${displayedProducts.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      Text(
        text = "Genuine Quality Guaranteed",
        style = MaterialTheme.typography.labelSmall,
        color = GoldPrimary
      )
    }

    // 4. Products Grid
    if (displayedProducts.isEmpty()) {
      EmptyStateView(
        icon = Icons.Default.Category,
        title = "No Products in this Category",
        subtitle = "We are constantly adding new items to Arshad Collection.",
        actionText = "Explore All Categories",
        onActionClick = {
          selectedCategory = categories.firstOrNull()
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
