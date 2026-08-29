package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val badge: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
  onFinishOnboarding: () -> Unit
) {
  val pages = listOf(
    OnboardingPage(
      title = "Discover Your Style",
      subtitle = "Explore curated collections of premium Men's & Women's Eastern wear, luxury shalwar kameez, and designer suits.",
      icon = Icons.Outlined.Checkroom,
      badge = "PREMIUM FASHION"
    ),
    OnboardingPage(
      title = "Quality Products at Great Prices",
      subtitle = "From Egyptian cotton bedsheets and blackout curtains to crockery and smart lifestyle gadgets, quality is guaranteed.",
      icon = Icons.Outlined.Diamond,
      badge = "STYLE • QUALITY • TRUST"
    ),
    OnboardingPage(
      title = "Shop Easily, Anywhere in Pakistan",
      subtitle = "Seamless ordering with full Cash on Delivery support, instant WhatsApp customer assistance, and transparent tracking.",
      icon = Icons.Outlined.ShoppingBag,
      badge = "NATIONWIDE SHOPPING"
    ),
    OnboardingPage(
      title = "Fast & Reliable Delivery",
      subtitle = "Prompt doorstep delivery to Karachi, Lahore, Islamabad, Faisalabad, Multan, and all cities across Pakistan.",
      icon = Icons.Outlined.LocalShipping,
      badge = "DOORSTEP CONVENIENCE"
    )
  )

  val pagerState = rememberPagerState(pageCount = { pages.size })
  val coroutineScope = rememberCoroutineScope()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(BlackMain)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Row with Brand & Skip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "ARSHAD COLLECTION",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            letterSpacing = 1.sp
          )
        }

        if (pagerState.currentPage < pages.size - 1) {
          TextButton(onClick = onFinishOnboarding) {
            Text(
              text = "Skip",
              color = TextSecondaryDark,
              style = MaterialTheme.typography.bodyMedium
            )
          }
        } else {
          Spacer(modifier = Modifier.width(48.dp))
        }
      }

      // Center Pager
      HorizontalPager(
        state = pagerState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) { index ->
        val page = pages[index]
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          // Luxury Icon Circle
          Box(
            modifier = Modifier
              .size(130.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  listOf(BlackSurfaceVariant, BlackMain)
                )
              )
              .border(1.5.dp, GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = page.icon,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(56.dp)
            )
          }

          Spacer(modifier = Modifier.height(28.dp))

          Surface(
            color = GoldPrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
          ) {
            Text(
              text = page.badge,
              color = GoldPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = page.title,
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondaryDark,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
          )
        }
      }

      // Bottom Row with Dots & Navigation CTA
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Dot Indicators
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(pages.size) { index ->
            val isSelected = pagerState.currentPage == index
            Box(
              modifier = Modifier
                .width(if (isSelected) 24.dp else 8.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSelected) GoldPrimary else BlackCardBorder)
            )
          }
        }

        // Action Button
        Button(
          onClick = {
            if (pagerState.currentPage < pages.size - 1) {
              coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
              }
            } else {
              onFinishOnboarding()
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = BlackMain
          )
        ) {
          Text(
            text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
