package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityHelper {

  private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
  private val USERNAME_REGEX = "^[a-zA-Z0-9_]{3,30}$".toRegex()

  fun isValidEmail(email: String): Boolean {
    val trimmed = email.trim()
    return trimmed.isNotEmpty() && EMAIL_REGEX.matches(trimmed)
  }

  fun isValidUsername(username: String): Boolean {
    val trimmed = username.trim()
    return trimmed.isNotEmpty() && USERNAME_REGEX.matches(trimmed)
  }

  fun isValidPassword(password: String): Boolean {
    return password.length >= 8
  }

  fun generateSalt(): String {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt.joinToString("") { "%02x".format(it) }
  }

  /**
   * Hashes a password securely using SHA-256 with a unique per-user cryptographic salt
   * and 1000 iterative rounds to prevent rainbow table attacks.
   */
  fun hashPassword(password: String, salt: String): String {
    var result = (password + salt).toByteArray(Charsets.UTF_8)
    val digest = MessageDigest.getInstance("SHA-256")
    for (i in 0 until 1000) {
      digest.update(result)
      result = digest.digest()
    }
    return result.joinToString("") { "%02x".format(it) }
  }

  fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
    val computedHash = hashPassword(password, salt)
    return computedHash == expectedHash
  }
}
