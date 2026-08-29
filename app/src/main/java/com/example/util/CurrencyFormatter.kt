package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
  fun format(amount: Double, currency: String = "Rs."): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return "$currency ${formatter.format(amount)}"
  }
}
