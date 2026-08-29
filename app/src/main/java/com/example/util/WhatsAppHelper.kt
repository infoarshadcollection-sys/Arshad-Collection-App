package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {
  const val DEFAULT_WHATSAPP_NUMBER = "923413399629" // Pakistani international format for 03413399629

  fun openWhatsApp(context: Context, message: String, phoneNumber: String = DEFAULT_WHATSAPP_NUMBER) {
    try {
      val cleanPhone = formatPhoneNumber(phoneNumber)
      val encodedMsg = URLEncoder.encode(message, "UTF-8")
      val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg")
      val intent = Intent(Intent.ACTION_VIEW, uri)
      context.startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(context, "Could not open WhatsApp. Please ensure WhatsApp is installed.", Toast.LENGTH_LONG).show()
    }
  }

  fun createProductInquiryMessage(
    productName: String,
    sku: String,
    size: String = "",
    color: String = "",
    quantity: Int = 1,
    price: Double
  ): String {
    val sizeText = if (size.isNotBlank()) " | Size: $size" else ""
    val colorText = if (color.isNotBlank()) " | Color: $color" else ""
    val qtyText = if (quantity > 1) " | Qty: $quantity" else ""
    val priceFormatted = CurrencyFormatter.format(price)

    return "Assalam-o-Alaikum Arshad Collection,\n\nI am interested in:\n• Product: $productName\n• SKU: $sku$sizeText$colorText$qtyText\n• Price: $priceFormatted\n\nPlease provide more details and availability."
  }

  fun createOrderInquiryMessage(orderNumber: String, total: Double): String {
    val totalFormatted = CurrencyFormatter.format(total)
    return "Assalam-o-Alaikum Arshad Collection,\n\nI would like information regarding my order:\n• Order Number: $orderNumber\n• Total: $totalFormatted\n\nPlease share the latest delivery status. JazakAllah."
  }

  fun createCartOrderMessage(itemsSummary: String, total: Double): String {
    val totalFormatted = CurrencyFormatter.format(total)
    return "Assalam-o-Alaikum Arshad Collection,\n\nI would like to place an order for the following items:\n$itemsSummary\n\nEstimated Total: $totalFormatted\n\nPlease confirm availability and payment details."
  }

  private fun formatPhoneNumber(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    return if (digits.startsWith("0")) {
      "92" + digits.substring(1)
    } else if (digits.startsWith("92")) {
      digits
    } else {
      "92$digits"
    }
  }
}
