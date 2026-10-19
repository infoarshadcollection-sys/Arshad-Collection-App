package com.example.data.supabase

import android.util.Log
import com.example.data.model.CustomerOrder
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Result data class tracking customer and admin email delivery status.
 */
data class OrderEmailResult(
  val customerEmailSent: Boolean = false,
  val customerEmailId: String? = null,
  val customerEmailError: String? = null,
  val adminEmailSent: Boolean = false,
  val adminEmailId: String? = null,
  val adminEmailError: String? = null
) {
  val isAnySent: Boolean get() = customerEmailSent || adminEmailSent
}

class SupabaseEmailService(private val config: SupabaseConfig) {

  companion object {
    private const val TAG = "SupabaseEmailService"
    const val ADMIN_EMAIL = "info.arshadcollection@gmail.com"
    const val EDGE_FUNCTION_ENDPOINT = "https://mqpssfovovuhfkqhytpg.supabase.co/functions/v1/resend-email"
  }

  private val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .writeTimeout(20, TimeUnit.SECONDS)
    .build()

  /**
   * Returns the Supabase Edge Function URL.
   * Prioritizes the user-specified function URL, or builds from configured Supabase URL.
   */
  fun getEdgeFunctionUrl(): String {
    val configuredUrl = config.getSupabaseUrl().trimEnd('/')
    return if (configuredUrl.isNotBlank() && !configuredUrl.contains("your-project.supabase.co")) {
      "$configuredUrl/functions/v1/resend-email"
    } else {
      EDGE_FUNCTION_ENDPOINT
    }
  }

  /**
   * Dispatches both customer confirmation and admin notification emails.
   */
  suspend fun sendOrderEmails(order: CustomerOrder): OrderEmailResult = withContext(Dispatchers.IO) {
    var customerSent = false
    var customerId: String? = null
    var customerErr: String? = null

    var adminSent = false
    var adminId: String? = null
    var adminErr: String? = null

    // 1. Send Customer Confirmation Email (if customer email is present & valid)
    val customerEmail = order.email.trim()
    if (customerEmail.isNotBlank() && customerEmail.contains("@") && customerEmail.contains(".")) {
      val custResult = sendOrderConfirmationToCustomer(order)
      custResult.onSuccess {
        customerSent = true
        customerId = it
        Log.d(TAG, "Customer confirmation email dispatched for order #${order.orderNumber} (id=$it)")
      }.onFailure {
        customerErr = it.message
        Log.e(TAG, "Failed to send customer confirmation email for order #${order.orderNumber}", it)
      }
    } else {
      customerErr = "Customer email not provided or invalid"
      Log.d(TAG, "Skipping customer email (empty or invalid: '$customerEmail')")
    }

    // 2. Send Admin Notification Email to info.arshadcollection@gmail.com
    val adminResult = sendOrderNotificationToAdmin(order)
    adminResult.onSuccess {
      adminSent = true
      adminId = it
      Log.d(TAG, "Admin order notification email dispatched for order #${order.orderNumber} (id=$it)")
    }.onFailure {
      adminErr = it.message
      Log.e(TAG, "Failed to send admin order notification email for order #${order.orderNumber}", it)
    }

    OrderEmailResult(
      customerEmailSent = customerSent,
      customerEmailId = customerId,
      customerEmailError = customerErr,
      adminEmailSent = adminSent,
      adminEmailId = adminId,
      adminEmailError = adminErr
    )
  }

  /**
   * Sends customer order confirmation email with required fields:
   * - Customer name
   * - Order number
   * - Ordered products
   * - Quantity
   * - Total amount
   * - Delivery address
   * - Order date
   * - Thank you message from Arshad Collection
   */
  suspend fun sendOrderConfirmationToCustomer(order: CustomerOrder): Result<String> = withContext(Dispatchers.IO) {
    val recipient = order.email.trim()
    if (recipient.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Customer email address is empty"))
    }

    val subject = "Order Confirmation #${order.orderNumber} | Arshad Collection"
    val htmlBody = buildCustomerEmailHtml(order)

    sendViaEdgeFunction(
      to = recipient,
      subject = subject,
      html = htmlBody
    )
  }

  /**
   * Sends admin order notification email to info.arshadcollection@gmail.com
   * with complete order details.
   */
  suspend fun sendOrderNotificationToAdmin(order: CustomerOrder): Result<String> = withContext(Dispatchers.IO) {
    val subject = "New Order Placed #${order.orderNumber} (${CurrencyFormatter.format(order.total)}) - Arshad Collection"
    val htmlBody = buildAdminEmailHtml(order)

    sendViaEdgeFunction(
      to = ADMIN_EMAIL,
      subject = subject,
      html = htmlBody
    )
  }

  /**
   * Sends a test email to verify Edge Function connectivity.
   */
  suspend fun sendTestEmail(recipient: String = ADMIN_EMAIL): Result<String> = withContext(Dispatchers.IO) {
    val subject = "Test Email from Arshad Collection | Supabase Resend Function"
    val html = """
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #D4AF37; border-radius: 8px; background-color: #121212; color: #FFFFFF;">
        <h2 style="color: #D4AF37; margin-top: 0;">Arshad Collection</h2>
        <p style="color: #E0E0E0; font-size: 15px;">Your Supabase Edge Function <code>resend-email</code> is working successfully!</p>
        <p style="color: #A0A0A0; font-size: 13px;">Tested at: ${SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())}</p>
        <hr style="border: none; border-top: 1px solid #333333; margin: 20px 0;" />
        <p style="color: #888888; font-size: 12px;">Arshad Collection Mobile Commerce &bull; Karachi, Pakistan</p>
      </div>
    """.trimIndent()

    sendViaEdgeFunction(to = recipient, subject = subject, html = html)
  }

  /**
   * Calls the Supabase Edge Function "resend-email".
   * Note: The RESEND_API_KEY is safely stored within the Supabase Edge Function environment
   * and is never exposed in the client code.
   */
  private fun sendViaEdgeFunction(to: String, subject: String, html: String): Result<String> {
    val functionUrl = getEdgeFunctionUrl()
    val anonKey = config.getAnonKey()

    try {
      val payload = JSONObject().apply {
        put("to", to.trim())
        put("subject", subject.trim())
        put("html", html)
      }

      val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

      val requestBuilder = Request.Builder()
        .url(functionUrl)
        .post(requestBody)
        .addHeader("Content-Type", "application/json")

      if (anonKey.isNotBlank()) {
        requestBuilder.addHeader("apikey", anonKey)
        requestBuilder.addHeader("Authorization", "Bearer $anonKey")
      }

      val request = requestBuilder.build()
      val response = httpClient.newCall(request).execute()

      val responseBody = response.body?.string().orEmpty()

      if (response.isSuccessful) {
        val json = try { JSONObject(responseBody) } catch (e: Exception) { JSONObject() }
        val id = json.optString("id", "sent_${System.currentTimeMillis()}")
        return Result.success(id)
      } else {
        val errorMsg = "Supabase Edge Function returned HTTP ${response.code}: $responseBody"
        Log.e(TAG, errorMsg)
        return Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error invoking Supabase Edge Function at $functionUrl", e)
      return Result.failure(e)
    }
  }

  /**
   * Generates a luxury branded HTML email for the Customer.
   */
  private fun buildCustomerEmailHtml(order: CustomerOrder): String {
    val orderDateFormatted = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

    val fullAddress = listOf(
      order.shippingAddress,
      order.area.takeIf { it.isNotBlank() },
      order.city.takeIf { it.isNotBlank() },
      order.postalCode.takeIf { it.isNotBlank() }
    ).filterNotNull().joinToString(", ")

    val itemsHtml = buildString {
      for (item in order.items) {
        val variantInfo = listOfNotNull(
          item.size.takeIf { it.isNotBlank() }?.let { "Size: $it" },
          item.color.takeIf { it.isNotBlank() }?.let { "Color: $it" }
        ).joinToString(" &bull; ")

        val subtotal = CurrencyFormatter.format(item.unitPrice * item.quantity)
        val unitPrice = CurrencyFormatter.format(item.unitPrice)

        append("""
          <tr>
            <td style="padding: 12px; border-bottom: 1px solid #2A2A2A; color: #FFFFFF; font-size: 14px;">
              <strong>${item.productName}</strong>
              ${if (variantInfo.isNotBlank()) "<div style='color: #C5A059; font-size: 12px; margin-top: 3px;'>$variantInfo</div>" else ""}
            </td>
            <td style="padding: 12px; border-bottom: 1px solid #2A2A2A; color: #CCCCCC; font-size: 14px; text-align: center;">
              ${item.quantity}
            </td>
            <td style="padding: 12px; border-bottom: 1px solid #2A2A2A; color: #CCCCCC; font-size: 14px; text-align: right;">
              $unitPrice
            </td>
            <td style="padding: 12px; border-bottom: 1px solid #2A2A2A; color: #FFFFFF; font-size: 14px; text-align: right; font-weight: bold;">
              $subtotal
            </td>
          </tr>
        """.trimIndent())
      }
    }

    val discountRow = if (order.discount > 0) {
      """
        <tr>
          <td colspan="3" style="padding: 8px 12px; text-align: right; color: #A0A0A0; font-size: 14px;">Discount:</td>
          <td style="padding: 8px 12px; text-align: right; color: #EF5350; font-size: 14px; font-weight: bold;">-${CurrencyFormatter.format(order.discount)}</td>
        </tr>
      """.trimIndent()
    } else ""

    val deliveryFeeText = if (order.deliveryFee <= 0.0) "FREE" else CurrencyFormatter.format(order.deliveryFee)

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Order Confirmation #${order.orderNumber}</title>
      </head>
      <body style="margin: 0; padding: 20px; background-color: #0D0D0D; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #E0E0E0;">
        <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 620px; background-color: #161616; border: 1px solid #2E2E2E; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.5);">
          
          <!-- Header Banner -->
          <tr>
            <td style="background: linear-gradient(135deg, #1C1A16 0%, #111111 100%); padding: 28px 24px; text-align: center; border-bottom: 2px solid #C5A059;">
              <h1 style="margin: 0; color: #D4AF37; font-size: 26px; letter-spacing: 2px; text-transform: uppercase; font-weight: 800;">ARSHAD COLLECTION</h1>
              <p style="margin: 6px 0 0 0; color: #A89F91; font-size: 13px; letter-spacing: 1px;">Luxury Eastern Wear &bull; Premium Home Essentials</p>
            </td>
          </tr>

          <!-- Confirmation Badge -->
          <tr>
            <td style="padding: 24px 24px 10px 24px;">
              <div style="background-color: #1F2A1E; border: 1px solid #2E5A34; border-radius: 8px; padding: 14px; text-align: center;">
                <span style="color: #8CF49C; font-size: 16px; font-weight: bold;">&#10003; Order Successfully Confirmed</span>
                <p style="margin: 4px 0 0 0; color: #A8CBB0; font-size: 13px;">Order Number: <strong style="color: #FFFFFF;">#${order.orderNumber}</strong></p>
              </div>
            </td>
          </tr>

          <!-- Greeting & Details -->
          <tr>
            <td style="padding: 16px 24px;">
              <p style="margin: 0 0 12px 0; font-size: 16px; color: #FFFFFF;">Dear <strong>${order.customerName.ifBlank { "Valued Customer" }}</strong>,</p>
              <p style="margin: 0 0 20px 0; font-size: 14px; color: #CCCCCC; line-height: 1.6;">
                Thank you for shopping with <strong>Arshad Collection</strong>! We have received your order and are preparing your items with the utmost care. Below are your full order details.
              </p>

              <!-- Order Summary Meta Box -->
              <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #1F1F1F; border-radius: 8px; padding: 14px; margin-bottom: 24px; border: 1px solid #2A2A2A;">
                <tr>
                  <td style="padding: 6px 10px; font-size: 13px; color: #8E8E8E;">Order Number:</td>
                  <td style="padding: 6px 10px; font-size: 13px; color: #D4AF37; font-weight: bold;">#${order.orderNumber}</td>
                </tr>
                <tr>
                  <td style="padding: 6px 10px; font-size: 13px; color: #8E8E8E;">Order Date:</td>
                  <td style="padding: 6px 10px; font-size: 13px; color: #E0E0E0;">$orderDateFormatted</td>
                </tr>
                <tr>
                  <td style="padding: 6px 10px; font-size: 13px; color: #8E8E8E;">Payment Method:</td>
                  <td style="padding: 6px 10px; font-size: 13px; color: #E0E0E0;">${order.paymentMethod}</td>
                </tr>
                <tr>
                  <td style="padding: 6px 10px; font-size: 13px; color: #8E8E8E; vertical-align: top;">Delivery Address:</td>
                  <td style="padding: 6px 10px; font-size: 13px; color: #FFFFFF; font-weight: 500;">$fullAddress</td>
                </tr>
              </table>

              <!-- Ordered Products Table -->
              <h3 style="margin: 0 0 12px 0; color: #D4AF37; font-size: 16px; letter-spacing: 0.5px;">Ordered Products</h3>
              <table width="100%" cellpadding="0" cellspacing="0" style="border-collapse: collapse; margin-bottom: 20px; background-color: #1B1B1B; border-radius: 8px; overflow: hidden; border: 1px solid #2A2A2A;">
                <thead>
                  <tr style="background-color: #242424; color: #C5A059; font-size: 12px; text-transform: uppercase;">
                    <th style="padding: 10px 12px; text-align: left;">Product</th>
                    <th style="padding: 10px 12px; text-align: center;">Qty</th>
                    <th style="padding: 10px 12px; text-align: right;">Unit Price</th>
                    <th style="padding: 10px 12px; text-align: right;">Total</th>
                  </tr>
                </thead>
                <tbody>
                  $itemsHtml
                  <tr>
                    <td colspan="3" style="padding: 10px 12px; text-align: right; color: #A0A0A0; font-size: 14px;">Subtotal:</td>
                    <td style="padding: 10px 12px; text-align: right; color: #FFFFFF; font-size: 14px;">${CurrencyFormatter.format(order.subtotal)}</td>
                  </tr>
                  $discountRow
                  <tr>
                    <td colspan="3" style="padding: 6px 12px; text-align: right; color: #A0A0A0; font-size: 14px;">Delivery Fee:</td>
                    <td style="padding: 6px 12px; text-align: right; color: #8CF49C; font-size: 14px;">$deliveryFeeText</td>
                  </tr>
                  <tr style="background-color: #242424;">
                    <td colspan="3" style="padding: 14px 12px; text-align: right; color: #D4AF37; font-size: 16px; font-weight: bold;">Grand Total:</td>
                    <td style="padding: 14px 12px; text-align: right; color: #D4AF37; font-size: 18px; font-weight: 800;">${CurrencyFormatter.format(order.total)}</td>
                  </tr>
                </tbody>
              </table>

              <!-- Thank You Message from Arshad Collection -->
              <div style="background-color: #1F1B14; border-left: 4px solid #D4AF37; padding: 16px; border-radius: 0 8px 8px 0; margin-bottom: 24px;">
                <p style="margin: 0; color: #E8DCC2; font-size: 14px; line-height: 1.6;">
                  <strong>A Heartfelt Thank You from Arshad Collection:</strong><br/>
                  We deeply value your trust in our brand. Each piece from our collection is crafted with passion and precision. Our delivery partner will contact you upon dispatch.
                </p>
              </div>

              <!-- Assistance Note -->
              <p style="margin: 0; font-size: 13px; color: #9E9E9E; line-height: 1.5;">
                Need help or want to track your order? Reply directly to this email or WhatsApp us at <strong style="color: #D4AF37;">0341-3399629</strong>.
              </p>
            </td>
          </tr>

          <!-- Footer -->
          <tr>
            <td style="background-color: #0F0F0F; padding: 20px; text-align: center; border-top: 1px solid #222222;">
              <p style="margin: 0 0 6px 0; color: #C5A059; font-size: 13px; font-weight: 600;">Arshad Collection Pakistan</p>
              <p style="margin: 0; color: #707070; font-size: 11px;">Karachi, Pakistan &bull; Phone: 0341-3399629 &bull; info.arshadcollection@gmail.com</p>
            </td>
          </tr>
        </table>
      </body>
      </html>
    """.trimIndent()
  }

  /**
   * Generates a detailed admin notification HTML email for info.arshadcollection@gmail.com
   * containing complete order details.
   */
  private fun buildAdminEmailHtml(order: CustomerOrder): String {
    val orderDateFormatted = SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date(order.createdAt))

    val itemsHtml = buildString {
      for (item in order.items) {
        val lineTotal = CurrencyFormatter.format(item.unitPrice * item.quantity)
        val unitPrice = CurrencyFormatter.format(item.unitPrice)
        append("""
          <tr>
            <td style="padding: 10px; border-bottom: 1px solid #E0E0E0; font-size: 13px; color: #111111;">
              <strong>${item.productName}</strong><br/>
              <span style="color: #666666; font-size: 11px;">SKU: ${item.sku.ifBlank { "N/A" }} | ID: ${item.productId}</span>
            </td>
            <td style="padding: 10px; border-bottom: 1px solid #E0E0E0; font-size: 13px; color: #333333; text-align: center;">
              ${item.size.ifBlank { "-" }} / ${item.color.ifBlank { "-" }}
            </td>
            <td style="padding: 10px; border-bottom: 1px solid #E0E0E0; font-size: 13px; color: #111111; text-align: center; font-weight: bold;">
              ${item.quantity}
            </td>
            <td style="padding: 10px; border-bottom: 1px solid #E0E0E0; font-size: 13px; color: #333333; text-align: right;">
              $unitPrice
            </td>
            <td style="padding: 10px; border-bottom: 1px solid #E0E0E0; font-size: 13px; color: #111111; text-align: right; font-weight: bold;">
              $lineTotal
            </td>
          </tr>
        """.trimIndent())
      }
    }

    val cleanPhone = order.phone.replace("[^0-9]".toRegex(), "")
    val waLink = if (cleanPhone.startsWith("0")) "92" + cleanPhone.substring(1) else cleanPhone

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>New Order #${order.orderNumber}</title>
      </head>
      <body style="margin: 0; padding: 20px; background-color: #F4F4F5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
        <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 640px; background-color: #FFFFFF; border: 1px solid #E4E4E7; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
          
          <!-- Header -->
          <tr>
            <td style="background-color: #18181B; padding: 20px 24px; border-bottom: 3px solid #D4AF37;">
              <h2 style="margin: 0; color: #D4AF37; font-size: 20px;">ARSHAD COLLECTION &bull; ADMIN ORDER ALERT</h2>
              <p style="margin: 4px 0 0 0; color: #A1A1AA; font-size: 13px;">New Customer Order Received &bull; Order #${order.orderNumber}</p>
            </td>
          </tr>

          <!-- Summary Alert -->
          <tr>
            <td style="padding: 20px 24px 0 24px;">
              <div style="background-color: #FEF3C7; border: 1px solid #F59E0B; border-radius: 6px; padding: 12px 16px;">
                <strong style="color: #92400E; font-size: 15px;">Action Required: Process Order #${order.orderNumber}</strong>
                <p style="margin: 4px 0 0 0; color: #78350F; font-size: 13px;">
                  Total: <strong>${CurrencyFormatter.format(order.total)}</strong> | Payment: <strong>${order.paymentMethod}</strong> (${order.paymentStatus})
                </p>
              </div>
            </td>
          </tr>

          <!-- Customer & Delivery Information Grid -->
          <tr>
            <td style="padding: 20px 24px;">
              <table width="100%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                <tr>
                  <td width="50%" style="vertical-align: top; padding-right: 12px;">
                    <div style="background-color: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 6px; padding: 14px;">
                      <h4 style="margin: 0 0 10px 0; color: #0F172A; font-size: 14px; text-transform: uppercase;">Customer Details</h4>
                      <p style="margin: 0 0 6px 0; font-size: 13px; color: #334155;"><strong>Name:</strong> ${order.customerName}</p>
                      <p style="margin: 0 0 6px 0; font-size: 13px; color: #334155;">
                        <strong>Phone:</strong> <a href="tel:${order.phone}" style="color: #2563EB;">${order.phone}</a>
                        ${if (waLink.isNotBlank()) " &bull; <a href='https://wa.me/$waLink' style='color: #059669; font-weight: bold;'>WhatsApp</a>" else ""}
                      </p>
                      <p style="margin: 0; font-size: 13px; color: #334155;"><strong>Email:</strong> ${order.email.ifBlank { "Not provided" }}</p>
                    </div>
                  </td>
                  <td width="50%" style="vertical-align: top; padding-left: 12px;">
                    <div style="background-color: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 6px; padding: 14px;">
                      <h4 style="margin: 0 0 10px 0; color: #0F172A; font-size: 14px; text-transform: uppercase;">Delivery Address</h4>
                      <p style="margin: 0 0 4px 0; font-size: 13px; color: #334155;"><strong>Address:</strong> ${order.shippingAddress}</p>
                      ${if (order.area.isNotBlank()) "<p style='margin: 0 0 4px 0; font-size: 13px; color: #334155;'><strong>Area:</strong> ${order.area}</p>" else ""}
                      <p style="margin: 0 0 4px 0; font-size: 13px; color: #334155;"><strong>City:</strong> ${order.city}</p>
                      ${if (order.postalCode.isNotBlank()) "<p style='margin: 0 0 4px 0; font-size: 13px; color: #334155;'><strong>Postal Code:</strong> ${order.postalCode}</p>" else ""}
                    </div>
                  </td>
                </tr>
              </table>

              ${if (order.deliveryInstructions.isNotBlank()) """
                <div style="margin-top: 14px; background-color: #F1F5F9; border-left: 4px solid #3B82F6; padding: 10px 14px; border-radius: 0 4px 4px 0;">
                  <strong style="color: #1E293B; font-size: 13px;">Customer Instructions / Notes:</strong>
                  <p style="margin: 4px 0 0 0; color: #475569; font-size: 13px;">${order.deliveryInstructions}</p>
                </div>
              """ else ""}

              <!-- Products Ordered -->
              <h4 style="margin: 24px 0 10px 0; color: #0F172A; font-size: 14px; text-transform: uppercase;">Ordered Items</h4>
              <table width="100%" cellpadding="0" cellspacing="0" style="border-collapse: collapse; border: 1px solid #E2E8F0; border-radius: 6px; overflow: hidden;">
                <thead>
                  <tr style="background-color: #F1F5F9; color: #475569; font-size: 12px; text-transform: uppercase;">
                    <th style="padding: 10px; text-align: left;">Product</th>
                    <th style="padding: 10px; text-align: center;">Size/Color</th>
                    <th style="padding: 10px; text-align: center;">Qty</th>
                    <th style="padding: 10px; text-align: right;">Unit</th>
                    <th style="padding: 10px; text-align: right;">Total</th>
                  </tr>
                </thead>
                <tbody>
                  $itemsHtml
                  <tr>
                    <td colspan="4" style="padding: 8px 10px; text-align: right; color: #64748B; font-size: 13px;">Subtotal:</td>
                    <td style="padding: 8px 10px; text-align: right; color: #0F172A; font-size: 13px; font-weight: 500;">${CurrencyFormatter.format(order.subtotal)}</td>
                  </tr>
                  ${if (order.discount > 0) """
                    <tr>
                      <td colspan="4" style="padding: 6px 10px; text-align: right; color: #EF4444; font-size: 13px;">Discount (${order.couponCode}):</td>
                      <td style="padding: 6px 10px; text-align: right; color: #EF4444; font-size: 13px; font-weight: bold;">-${CurrencyFormatter.format(order.discount)}</td>
                    </tr>
                  """ else ""}
                  <tr>
                    <td colspan="4" style="padding: 6px 10px; text-align: right; color: #64748B; font-size: 13px;">Delivery Fee:</td>
                    <td style="padding: 6px 10px; text-align: right; color: #0F172A; font-size: 13px;">${CurrencyFormatter.format(order.deliveryFee)}</td>
                  </tr>
                  <tr style="background-color: #F8FAFC;">
                    <td colspan="4" style="padding: 12px 10px; text-align: right; color: #0F172A; font-size: 15px; font-weight: bold;">Grand Total Payable:</td>
                    <td style="padding: 12px 10px; text-align: right; color: #16A34A; font-size: 16px; font-weight: 800;">${CurrencyFormatter.format(order.total)}</td>
                  </tr>
                </tbody>
              </table>

              <!-- Order Metadata -->
              <div style="margin-top: 20px; font-size: 12px; color: #94A3B8; line-height: 1.6;">
                <p style="margin: 0;"><strong>Date:</strong> $orderDateFormatted &bull; <strong>Tracking:</strong> ${order.trackingNumber.ifBlank { "Unassigned" }} &bull; <strong>Status:</strong> ${order.orderStatus}</p>
              </div>
            </td>
          </tr>

          <!-- Footer -->
          <tr>
            <td style="background-color: #F8FAFC; padding: 14px 24px; text-align: center; border-top: 1px solid #E2E8F0;">
              <p style="margin: 0; color: #94A3B8; font-size: 11px;">Arshad Collection Store Management &bull; Supabase Edge Functions</p>
            </td>
          </tr>
        </table>
      </body>
      </html>
    """.trimIndent()
  }
}
