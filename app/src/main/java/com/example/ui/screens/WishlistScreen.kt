package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun WishlistScreen(
  viewModel: ShopViewModel,
  onProductClick: (Product) -> Unit,
  onExploreClick: () -> Unit
) {
  val wishlistItems by viewModel.wishlistItems.collectAsState()
  val wishlistedIds = remember(wishlistItems) { wishlistItems.map { it.product.id }.toSet() }

  if (wishlistItems.isEmpty()) {
    EmptyStateView(
      icon = Icons.Outlined.FavoriteBorder,
      title = "Your Wishlist is Empty",
      subtitle = "Save your favorite luxury suits, bedsheets, crockery, and gadgets here for quick access.",
      actionText = "Explore Collection",
      onActionClick = onExploreClick
    )
  } else {
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(wishlistItems) { item ->
        ProductCard(
          product = item.product,
          isWishlisted = true,
          onProductClick = { onProductClick(item.product) },
          onWishlistToggle = { viewModel.toggleWishlist(item.product.id) },
          onAddToCart = { viewModel.addToCart(item.product) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
