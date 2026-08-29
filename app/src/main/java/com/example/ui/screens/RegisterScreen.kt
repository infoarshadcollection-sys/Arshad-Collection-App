package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArshadBrandLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.SecurityHelper

@Composable
fun RegisterScreen(
  viewModel: ShopViewModel,
  onNavigateToLogin: () -> Unit,
  onRegisterSuccess: () -> Unit,
  onContinueAsGuest: () -> Unit
) {
  var email by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }

  var clientError by remember { mutableStateOf<String?>(null) }

  val authLoading by viewModel.authLoading.collectAsState()
  val authError by viewModel.authError.collectAsState()
  val focusManager = LocalFocusManager.current

  fun handleRegister() {
    clientError = null
    viewModel.clearAuthMessages()

    val trimmedEmail = email.trim()
    val trimmedUsername = username.trim()

    // 1. Email validation
    if (trimmedEmail.isEmpty()) {
      clientError = "Email is required."
      return
    }
    if (!SecurityHelper.isValidEmail(trimmedEmail)) {
      clientError = "Please enter a valid email address."
      return
    }

    // 2. Username validation
    if (trimmedUsername.isEmpty()) {
      clientError = "Username is required."
      return
    }
    if (!SecurityHelper.isValidUsername(trimmedUsername)) {
      clientError = "Username must be 3-30 alphanumeric characters."
      return
    }

    // 3. Password validation
    if (password.isEmpty()) {
      clientError = "Password is required."
      return
    }
    if (!SecurityHelper.isValidPassword(password)) {
      clientError = "Password must be at least 8 characters."
      return
    }

    focusManager.clearFocus()
    viewModel.register(
      email = trimmedEmail,
      username = trimmedUsername,
      password = password,
      onSuccess = onRegisterSuccess
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Official Arshad Collection Logo (Prominently centered at top)
      ArshadBrandLogo(
        size = 92,
        modifier = Modifier.testTag("register_official_logo")
      )

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "Create Account",
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )

      Text(
        text = "Join Arshad Collection • Style • Quality • Trust",
        color = GoldPrimary,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
      )

      // Registration Form Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 480.dp),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Error Message Display
          val displayError = clientError ?: authError
          AnimatedVisibility(
            visible = displayError != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            if (displayError != null) {
              Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = displayError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }

          // Field 1: Email
          OutlinedTextField(
            value = email,
            onValueChange = {
              email = it
              clientError = null
            },
            label = { Text("Email Address") },
            placeholder = { Text("example@domain.com") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = "Email",
                tint = GoldPrimary
              )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
              onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = Color.DarkGray,
              focusedLabelColor = GoldPrimary,
              unfocusedLabelColor = TextSecondaryDark,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              cursorColor = GoldPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("register_email_input")
          )

          // Field 2: Username
          OutlinedTextField(
            value = username,
            onValueChange = {
              username = it
              clientError = null
            },
            label = { Text("Username") },
            placeholder = { Text("Letters & numbers (e.g. arshad_99)") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Username",
                tint = GoldPrimary
              )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
              onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = Color.DarkGray,
              focusedLabelColor = GoldPrimary,
              unfocusedLabelColor = TextSecondaryDark,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              cursorColor = GoldPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("register_username_input")
          )

          // Field 3: Password
          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              clientError = null
            },
            label = { Text("Password (Min. 8 characters)") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Password",
                tint = GoldPrimary
              )
            },
            trailingIcon = {
              IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
                modifier = Modifier.testTag("register_toggle_password")
              ) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                  contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                  tint = GoldPrimary
                )
              }
            },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = { handleRegister() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = Color.DarkGray,
              focusedLabelColor = GoldPrimary,
              unfocusedLabelColor = TextSecondaryDark,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              cursorColor = GoldPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("register_password_input")
          )

          // Register Button: "Create Account"
          Button(
            onClick = { handleRegister() },
            enabled = !authLoading,
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = BlackMain,
              disabledContainerColor = GoldPrimary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("register_create_account_button")
          ) {
            if (authLoading) {
              CircularProgressIndicator(
                color = BlackMain,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
              )
            } else {
              Text(
                text = "Create Account",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Already have an account? Login
      Row(
        modifier = Modifier.clickable { onNavigateToLogin() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Already have an account? ",
          color = TextSecondaryDark,
          fontSize = 14.sp
        )
        Text(
          text = "Login",
          color = GoldPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.testTag("register_to_login_button")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Continue as Guest option
      TextButton(
        onClick = { onContinueAsGuest() },
        modifier = Modifier.testTag("register_guest_button")
      ) {
        Text(
          text = "Continue as Guest",
          color = TextSecondaryDark,
          fontSize = 13.sp
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
