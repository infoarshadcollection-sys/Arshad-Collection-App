package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ai.ChatMessage
import com.example.data.ai.ChatSender
import com.example.data.model.Product
import com.example.ui.components.ArshadBrandLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FloatingAiAssistantButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Surface(
    onClick = onClick,
    modifier = modifier
      .padding(bottom = 16.dp, end = 16.dp)
      .shadow(12.dp, RoundedCornerShape(28.dp)),
    shape = RoundedCornerShape(28.dp),
    color = Color.Transparent
  ) {
    Box(
      modifier = Modifier
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(
              GoldPrimary,
              GoldDark,
              GoldLight
            )
          ),
          shape = RoundedCornerShape(28.dp)
        )
        .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
        .padding(horizontal = 16.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(BlackMain.copy(alpha = 0.25f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Assistant",
            tint = BlackMain,
            modifier = Modifier.size(18.dp)
          )
        }

        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Ask AI Assistant",
              color = BlackMain,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp
            )
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF00C853))
            )
          }
          Text(
            text = "English • اردو • Roman Urdu",
            color = BlackMain.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium,
            fontSize = 9.5.sp
          )
        }
      }
    }
  }
}

@Composable
fun AiAssistantDialog(
  viewModel: ShopViewModel,
  onDismiss: () -> Unit,
  onProductClick: (Product) -> Unit
) {
  val messages by viewModel.aiMessages.collectAsState()
  val isThinking by viewModel.isAiThinking.collectAsState()
  val settings by viewModel.appSettings.collectAsState()
  val suggestedPrompts = viewModel.aiSuggestedPrompts
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current

  var textInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  // Track IME / software keyboard height & open status dynamically
  val imeInsets = WindowInsets.ime
  val density = androidx.compose.ui.platform.LocalDensity.current
  val isKeyboardOpen = imeInsets.getBottom(density) > 0

  // Handle hardware / gesture Back to close the assistant
  BackHandler(enabled = true) {
    onDismiss()
  }

  // Auto-scroll to bottom when new messages arrive, AI is thinking, or keyboard opens
  LaunchedEffect(messages.size, isThinking, isKeyboardOpen) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  // Root Full-Screen Overlay with Strict Safe-Area Insets (Status bar, Navigation Bar, Keyboard IME)
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.72f))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        onDismiss()
      }
      .windowInsetsPadding(WindowInsets.statusBars)
      .windowInsetsPadding(WindowInsets.navigationBars)
      .windowInsetsPadding(WindowInsets.ime)
      .padding(horizontal = 10.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null
        ) {
          // Absorb taps inside the card so clicks don't dismiss the dialog
        },
      shape = RoundedCornerShape(20.dp),
      color = BlackSurface,
      border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
      shadowElevation = 16.dp
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // 1. FIXED TOP HEADER
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(BlackSurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                  brush = Brush.linearGradient(
                    colors = listOf(GoldPrimary, GoldDark)
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = BlackMain,
                modifier = Modifier.size(22.dp)
              )
            }

            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "Arshad AI Assistant",
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
                Box(
                  modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676))
                )
              }
              Text(
                text = "Stylist • 24/7 Support • Urdu / English",
                color = TextSecondaryDark,
                fontSize = 11.sp
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
              onClick = { viewModel.clearAiChat() },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = "Clear Chat",
                tint = TextSecondaryDark,
                modifier = Modifier.size(20.dp)
              )
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        HorizontalDivider(color = BlackDivider)

        // 2. SCROLLABLE CHAT MESSAGES AREA (Takes all flexible height)
        LazyColumn(
          state = listState,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(messages, key = { it.id }) { message ->
            ChatMessageBubble(
              message = message,
              onProductClick = {
                onDismiss()
                onProductClick(it)
              },
              onWhatsAppClick = {
                WhatsAppHelper.openWhatsApp(
                  context = context,
                  message = "Assalam-o-Alaikum Arshad Collection, I need assistance regarding customer support and product inquiries.",
                  phoneNumber = settings.whatsappNumber
                )
              }
            )
          }

          if (isThinking) {
            item {
              AiThinkingIndicator()
            }
          }
        }

        // 3. QUICK SUGGESTION PROMPTS ROW (Visible on introductory state when keyboard is closed)
        if (messages.size <= 2 && !isKeyboardOpen) {
          Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text(
              text = "SUGGESTED QUESTIONS:",
              color = TextSecondaryDark,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(suggestedPrompts) { prompt ->
                SuggestionChip(
                  onClick = {
                    viewModel.sendAiUserMessage(prompt)
                    keyboardController?.hide()
                  },
                  label = {
                    Text(
                      text = prompt,
                      fontSize = 11.sp,
                      color = Color.White
                    )
                  },
                  colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = BlackSurfaceVariant
                  ),
                  border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                  shape = RoundedCornerShape(16.dp)
                )
              }
            }
          }
        }

        HorizontalDivider(color = BlackDivider)

        // 4. FIXED BOTTOM INPUT BAR (Always visible above navigation buttons & keyboard)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(BlackSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          IconButton(
            onClick = {
              WhatsAppHelper.openWhatsApp(
                context = context,
                message = "Assalam-o-Alaikum Arshad Collection, I would like to inquire with customer support.",
                phoneNumber = settings.whatsappNumber
              )
            },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color(0xFF25D366).copy(alpha = 0.15f))
          ) {
            Icon(
              imageVector = Icons.Default.Chat,
              contentDescription = "WhatsApp Care",
              tint = Color(0xFF25D366),
              modifier = Modifier.size(20.dp)
            )
          }

          OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = {
              Text(
                text = "Type in English, Urdu or Roman Urdu...",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 46.dp, max = 100.dp),
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = BlackCard,
              unfocusedContainerColor = BlackCard,
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = BlackCardBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              cursorColor = GoldPrimary
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
              onSend = {
                if (textInput.isNotBlank() && !isThinking) {
                  val q = textInput
                  textInput = ""
                  viewModel.sendAiUserMessage(q)
                  keyboardController?.hide()
                }
              }
            )
          )

          IconButton(
            onClick = {
              if (textInput.isNotBlank() && !isThinking) {
                val q = textInput
                textInput = ""
                viewModel.sendAiUserMessage(q)
                keyboardController?.hide()
              }
            },
            enabled = textInput.isNotBlank() && !isThinking,
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(
                if (textInput.isNotBlank() && !isThinking) GoldPrimary else BlackCardBorder
              )
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send",
              tint = if (textInput.isNotBlank() && !isThinking) BlackMain else TextSecondaryDark,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onProductClick: (Product) -> Unit,
  onWhatsAppClick: () -> Unit
) {
  val isUser = message.sender == ChatSender.USER
  val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
  val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Row(
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
      modifier = Modifier.fillMaxWidth(if (isUser) 0.9f else 0.98f)
    ) {
      if (!isUser) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(GoldPrimary)
            .padding(4.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = BlackMain,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
      }

      Surface(
        shape = RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isUser) 16.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 16.dp
        ),
        color = if (isUser) GoldPrimary else BlackSurfaceVariant,
        border = if (!isUser) BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)) else null,
        tonalElevation = 2.dp,
        modifier = Modifier.widthIn(max = 320.dp)
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          Text(
            text = message.text,
            color = if (isUser) BlackMain else Color.White,
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
            fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
          )

          // Recommended Products List
          if (message.recommendedProducts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "RECOMMENDED PRODUCTS:",
              color = if (isUser) BlackMain else GoldPrimary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              message.recommendedProducts.forEach { product ->
                AiProductMiniCard(
                  product = product,
                  onClick = { onProductClick(product) }
                )
              }
            }
          }

          // WhatsApp direct action button if advised
          if (message.showWhatsAppButton) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = onWhatsAppClick,
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Chat on WhatsApp Care",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = formattedTime,
            color = if (isUser) BlackMain.copy(alpha = 0.6f) else TextSecondaryDark,
            fontSize = 9.sp,
            modifier = Modifier.align(Alignment.End)
          )
        }
      }
    }
  }
}

@Composable
fun AiProductMiniCard(
  product: Product,
  onClick: () -> Unit
) {
  val context = LocalContext.current
  val firstImage = product.images.firstOrNull() ?: ""
  val imageResId = remember(firstImage) {
    if (firstImage.isNotBlank()) {
      context.resources.getIdentifier(firstImage, "drawable", context.packageName)
    } else 0
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable { onClick() },
    color = BlackCard,
    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(BlackSurfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        if (imageResId != 0) {
          AsyncImage(
            model = imageResId,
            contentDescription = product.name,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          ArshadBrandLogo(size = 32)
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = product.name,
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = CurrencyFormatter.format(product.currentPrice),
            color = GoldPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
          )
          if (product.isInStock) {
            Text(
              text = "• In Stock",
              color = Color(0xFF00E676),
              fontSize = 10.sp
            )
          } else {
            Text(
              text = "• Out of Stock",
              color = RedDiscount,
              fontSize = 10.sp
            )
          }
        }
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "View",
        tint = GoldPrimary,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
fun AiThinkingIndicator() {
  val infiniteTransition = rememberInfiniteTransition(label = "dots")
  val dot1Alpha by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 0), repeatMode = RepeatMode.Reverse),
    label = "dot1"
  )
  val dot2Alpha by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 200), repeatMode = RepeatMode.Reverse),
    label = "dot2"
  )
  val dot3Alpha by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 400), repeatMode = RepeatMode.Reverse),
    label = "dot3"
  )

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(GoldPrimary.copy(alpha = 0.2f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = GoldPrimary,
        modifier = Modifier.size(14.dp)
      )
    }

    Surface(
      shape = RoundedCornerShape(12.dp),
      color = BlackSurfaceVariant,
      border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("AI Stylist is thinking", color = TextSecondaryDark, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GoldPrimary.copy(alpha = dot1Alpha)))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GoldPrimary.copy(alpha = dot2Alpha)))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GoldPrimary.copy(alpha = dot3Alpha)))
      }
    }
  }
}
