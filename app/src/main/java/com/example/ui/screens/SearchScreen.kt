package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.FilterState
import com.example.ui.viewmodel.ShopViewModel
import com.example.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
  viewModel: ShopViewModel,
  onProductClick: (Product) -> Unit
) {
  val searchQuery by viewModel.searchQuery.collectAsState()
  val filteredProducts by viewModel.filteredProducts.collectAsState()
  val recentSearches by viewModel.recentSearches.collectAsState()
  val categories by viewModel.categories.collectAsState()
  val currentFilter by viewModel.searchFilter.collectAsState()
  val currentSort by viewModel.sortOption.collectAsState()
  val wishlistItems by viewModel.wishlistItems.collectAsState()
  val wishlistedIds = remember(wishlistItems) { wishlistItems.map { it.product.id }.toSet() }

  var showFilterBottomSheet by remember { mutableStateOf(false) }
  var showSortBottomSheet by remember { mutableStateOf(false) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val sortSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // 1. Search Input Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.performSearch(it) },
        placeholder = { Text("Search products, brands, materials...") },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GoldPrimary)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.clearSearch() }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondaryDark)
            }
          }
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GoldPrimary,
          unfocusedBorderColor = BlackCardBorder,
          focusedContainerColor = BlackSurfaceVariant,
          unfocusedContainerColor = BlackSurfaceVariant,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          cursorColor = GoldPrimary
        ),
        singleLine = true
      )

      // Filter Button
      IconButton(
        onClick = { showFilterBottomSheet = true },
        modifier = Modifier
          .size(52.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(if (currentFilter != FilterState()) GoldPrimary else BlackSurfaceVariant)
          .border(1.dp, if (currentFilter != FilterState()) GoldPrimary else BlackCardBorder, RoundedCornerShape(12.dp))
      ) {
        Icon(
          imageVector = Icons.Outlined.Tune,
          contentDescription = "Filter",
          tint = if (currentFilter != FilterState()) BlackMain else GoldPrimary
        )
      }
    }

    // 2. Sort & Active Filters Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${filteredProducts.size} results found",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryDark
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable { showSortBottomSheet = true }
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Sort,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = currentSort.displayName,
          style = MaterialTheme.typography.labelSmall,
          color = GoldPrimary,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // 3. Recent Searches & Popular Tags (When query is blank & default state)
    if (searchQuery.isBlank() && currentFilter == FilterState()) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Popular Searches
        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Popular Searches",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              viewModel.popularSearches.take(3).forEach { term ->
                SuggestionChip(
                  onClick = { viewModel.performSearch(term) },
                  label = { Text(term) },
                  colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = BlackSurfaceVariant,
                    labelColor = Color.White
                  ),
                  border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = BlackCardBorder
                  )
                )
              }
            }
          }
        }

        // Recent searches
        if (recentSearches.isNotEmpty()) {
          item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Recent Searches",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )

                Text(
                  text = "Clear",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMutedDark,
                  modifier = Modifier.clickable { viewModel.recentSearches.value = emptyList() }
                )
              }

              recentSearches.forEach { search ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.performSearch(search) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    text = search,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                  )
                  Icon(
                    imageVector = Icons.Default.NorthWest,
                    contentDescription = null,
                    tint = TextMutedDark,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }

        // Quick Products Grid
        item {
          Text(
            text = "Explore Our Products",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        items(filteredProducts.chunked(2)) { pair ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            pair.forEach { prod ->
              ProductCard(
                product = prod,
                isWishlisted = wishlistedIds.contains(prod.id),
                onProductClick = { onProductClick(prod) },
                onWishlistToggle = { viewModel.toggleWishlist(prod.id) },
                onAddToCart = { viewModel.addToCart(prod) },
                modifier = Modifier.weight(1f)
              )
            }
            if (pair.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    } else {
      // Results Grid
      if (filteredProducts.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.SearchOff,
          title = "No Products Found",
          subtitle = "We couldn't find any matches for \"$searchQuery\". Try adjusting your keywords or filters.",
          actionText = "Reset Filters",
          onActionClick = {
            viewModel.clearSearch()
            viewModel.resetFilter()
          }
        )
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredProducts) { product ->
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

  // Filter Bottom Sheet
  if (showFilterBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = { showFilterBottomSheet = false },
      sheetState = sheetState,
      containerColor = BlackSurface,
      tonalElevation = 8.dp
    ) {
      var tempFilter by remember { mutableStateOf(currentFilter) }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Filter Products",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary
          )

          TextButton(onClick = {
            tempFilter = FilterState()
          }) {
            Text("Reset All", color = TextSecondaryDark)
          }
        }

        Divider(color = BlackDivider)

        // 1. In Stock Only Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("In Stock Only", color = Color.White, style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = tempFilter.inStockOnly,
            onCheckedChange = { tempFilter = tempFilter.copy(inStockOnly = it) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = GoldPrimary,
              checkedTrackColor = GoldDark
            )
          )
        }

        // 2. Discounted Only Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Discounted / On Sale Only", color = Color.White, style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = tempFilter.discountOnly,
            onCheckedChange = { tempFilter = tempFilter.copy(discountOnly = it) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = GoldPrimary,
              checkedTrackColor = GoldDark
            )
          )
        }

        // 3. Category Filter
        Text("Category", color = GoldLight, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          item {
            FilterChip(
              selected = tempFilter.selectedCategoryId == null,
              onClick = { tempFilter = tempFilter.copy(selectedCategoryId = null) },
              label = { Text("All Categories") }
            )
          }
          items(categories) { cat ->
            FilterChip(
              selected = tempFilter.selectedCategoryId == cat.id,
              onClick = {
                tempFilter = tempFilter.copy(
                  selectedCategoryId = if (tempFilter.selectedCategoryId == cat.id) null else cat.id
                )
              },
              label = { Text(cat.name) }
            )
          }
        }

        // 4. Price Slider
        Text(
          text = "Price Range: Rs. ${tempFilter.minPrice.toInt()} - Rs. ${tempFilter.maxPrice.toInt()}",
          color = GoldLight,
          fontWeight = FontWeight.Bold
        )
        Slider(
          value = tempFilter.maxPrice.toFloat(),
          onValueChange = { tempFilter = tempFilter.copy(maxPrice = it.toDouble()) },
          valueRange = 1000f..25000f,
          steps = 24,
          colors = SliderDefaults.colors(
            thumbColor = GoldPrimary,
            activeTrackColor = GoldPrimary
          )
        )

        // Apply Button
        Button(
          onClick = {
            viewModel.updateFilter(tempFilter)
            showFilterBottomSheet = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = BlackMain
          )
        ) {
          Text("Apply Filters", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Sort Bottom Sheet
  if (showSortBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = { showSortBottomSheet = false },
      sheetState = sortSheetState,
      containerColor = BlackSurface,
      tonalElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Sort By",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = GoldPrimary
        )

        Divider(color = BlackDivider)

        SortOption.values().forEach { option ->
          val isSelected = currentSort == option
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable {
                viewModel.sortOption.value = option
                showSortBottomSheet = false
              }
              .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = option.displayName,
              style = MaterialTheme.typography.bodyLarge,
              color = if (isSelected) GoldPrimary else Color.White,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = GoldPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
