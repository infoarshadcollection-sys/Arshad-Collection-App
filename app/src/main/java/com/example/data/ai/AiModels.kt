package com.example.data.ai

import com.example.data.model.Product
import java.util.UUID

enum class ChatSender {
  USER,
  ASSISTANT,
  SYSTEM
}

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val sender: ChatSender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val recommendedProducts: List<Product> = emptyList(),
  val isError: Boolean = false,
  val showWhatsAppButton: Boolean = false
)
