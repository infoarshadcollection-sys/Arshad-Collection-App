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
fun LoginScreen(
  viewModel: ShopViewModel,
  onNavigateToRegister: () -> Unit,
  onLoginSuccess: () -> Unit,
  onContinueAsGuest: () -> Unit
) {
  var email by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }

  var clientError by remember { mutableStateOf<String?>(null) }
  var showForgotPasswordDialog by remember { mutableStateOf(false) }
  var forgotEmail by remember { mutableStateOf("") }
  var forgotMessage by remember { mutableStateOf<String?>(null) }

  val authLoading by viewModel.authLoading.collectAsState()
  val authError by viewModel.authError.collectAsState()
  val focusManager = LocalFocusManager.current

  fun handleLogin() {
    clientError = null
    viewModel.clearAuthMessages()

    val cleanEmail = email.trim()
    val cleanUsername = username.trim()

    // Enforce all 3 fields required
    if (cleanEmail.isEmpty() || cleanUsername.isEmpty() || password.isEmpty()) {
      clientError = "Email, username or password is incorrect."
      return
    }

    focusManager.clearFocus()
    viewModel.login(
      email = cleanEmail,
      username = cleanUsername,
      password = password,
      onSuccess = onLoginSuccess
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
        modifier = Modifier.testTag("login_official_logo")
      )

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "Customer Login",
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )

      Text(
        text = "Welcome to Arshad Collection • Style • Quality • Trust",
        color = GoldPrimary,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
      )

      // Login Card with exactly 3 fields: Email, Username, Password
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
          // Error Display
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
            label = { Text("Email") },
            placeholder = { Text("Registered email address") },
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
              .testTag("login_email_input")
          )

          // Field 2: Username
          OutlinedTextField(
            value = username,
            onValueChange = {
              username = it
              clientError = null
            },
            label = { Text("Username") },
            placeholder = { Text("Your registered username") },
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
              .testTag("login_username_input")
          )

          // Field 3: Password
          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              clientError = null
            },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
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
                modifier = Modifier.testTag("login_toggle_password")
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
              onDone = { handleLogin() }
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
              .testTag("login_password_input")
          )

          // Forgot Password Link
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Text(
              text = "Forgot Password?",
              color = GoldPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier
                .clickable {
                  forgotEmail = email.trim()
                  forgotMessage = null
                  showForgotPasswordDialog = true
                }
                .testTag("login_forgot_password_button")
            )
          }

          // Login Button: "Login"
          Button(
            onClick = { handleLogin() },
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
              .testTag("login_submit_button")
          ) {
            if (authLoading) {
              CircularProgressIndicator(
                color = BlackMain,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
              )
            } else {
              Text(
                text = "Login",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Don't have an account? Create Account
      Row(
        modifier = Modifier.clickable { onNavigateToRegister() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Don't have an account? ",
          color = TextSecondaryDark,
          fontSize = 14.sp
        )
        Text(
          text = "Create Account",
          color = GoldPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.testTag("login_to_register_button")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Guest access option
      TextButton(
        onClick = { onContinueAsGuest() },
        modifier = Modifier.testTag("login_guest_button")
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

  // Forgot Password Dialog
  if (showForgotPasswordDialog) {
    var dialogError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showForgotPasswordDialog = false },
      containerColor = BlackCard,
      shape = RoundedCornerShape(16.dp),
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.LockReset,
            contentDescription = null,
            tint = GoldPrimary
          )
          Text(
            text = "Reset Password",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (forgotMessage != null) {
            Surface(
              color = GoldPrimary.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = forgotMessage!!,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.padding(12.dp)
              )
            }
          } else {
            Text(
              text = "Enter your registered email address to receive password reset instructions.",
              color = TextSecondaryDark,
              fontSize = 13.sp
            )

            if (dialogError != null) {
              Text(
                text = dialogError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
              )
            }

            OutlinedTextField(
              value = forgotEmail,
              onValueChange = {
                forgotEmail = it
                dialogError = null
              },
              label = { Text("Registered Email") },
              placeholder = { Text("name@example.com") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                .testTag("forgot_password_email_input")
            )
          }
        }
      },
      confirmButton = {
        if (forgotMessage == null) {
          Button(
            onClick = {
              val clean = forgotEmail.trim()
              if (clean.isEmpty() || !SecurityHelper.isValidEmail(clean)) {
                dialogError = "Please enter a valid email address."
                return@Button
              }
              viewModel.requestPasswordReset(clean) { resultMsg ->
                forgotMessage = resultMsg
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = BlackMain
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("forgot_password_submit_button")
          ) {
            Text("Reset Password", fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = { showForgotPasswordDialog = false },
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = BlackMain
            ),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Done", fontWeight = FontWeight.Bold)
          }
        }
      },
      dismissButton = {
        if (forgotMessage == null) {
          TextButton(
            onClick = { showForgotPasswordDialog = false }
          ) {
            Text("Cancel", color = TextSecondaryDark)
          }
        }
      }
    )
  }
}
