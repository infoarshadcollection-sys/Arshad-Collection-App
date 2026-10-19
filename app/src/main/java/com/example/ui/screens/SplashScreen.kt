package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit
) {
  val transition = rememberInfiniteTransition(label = "gold_glow")
  val pulseScale by transition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  var startAnimation by remember { mutableStateOf(false) }
  val alphaAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = tween(durationMillis = 1000),
    label = "alpha"
  )

  LaunchedEffect(Unit) {
    startAnimation = true
    delay(2400)
    onSplashFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .padding(24.dp)
        .alpha(alphaAnim)
        .scale(pulseScale)
    ) {
      // Official Original Arshad Collection Logo (Exact uploaded graphic, 1:1 fit)
      Box(
        modifier = Modifier
          .fillMaxWidth(0.82f)
          .aspectRatio(1f),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = com.example.R.drawable.arshad_official_logo),
          contentDescription = "Official Arshad Collection Logo",
          modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f),
          contentScale = ContentScale.Fit
        )
      }
    }

    // Bottom Pakistani delivery indicator
    Text(
      text = "Delivering Quality Across Pakistan 🇵🇰",
      color = GoldLight.copy(alpha = 0.8f),
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 24.dp)
    )
  }
}
