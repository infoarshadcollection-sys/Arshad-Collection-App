package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class SupabaseConfig(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("arshad_supabase_config", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_URL = "supabase_url"
    private const val KEY_ANON_KEY = "supabase_anon_key"
    private const val KEY_STORAGE_BUCKET = "supabase_storage_bucket"
    const val DEFAULT_STORAGE_BUCKET = "products"
  }

  fun getSupabaseUrl(): String {
    val saved = prefs.getString(KEY_URL, null)
    if (!saved.isNullOrBlank()) return cleanUrl(saved)

    // Fallback to BuildConfig generated from .env / .env.example
    return try {
      val buildUrl = BuildConfig.SUPABASE_URL
      if (buildUrl.isNotBlank() && !buildUrl.contains("your-project.supabase.co")) {
        cleanUrl(buildUrl)
      } else {
        saved ?: ""
      }
    } catch (e: Throwable) {
      saved ?: ""
    }
  }

  fun getAnonKey(): String {
    val saved = prefs.getString(KEY_ANON_KEY, null)
    if (!saved.isNullOrBlank()) return saved.trim()

    return try {
      val buildKey = BuildConfig.SUPABASE_ANON_KEY
      if (buildKey.isNotBlank() && !buildKey.contains("placeholder_anon_key")) {
        buildKey.trim()
      } else {
        saved ?: ""
      }
    } catch (e: Throwable) {
      saved ?: ""
    }
  }

  fun getStorageBucket(): String {
    return prefs.getString(KEY_STORAGE_BUCKET, DEFAULT_STORAGE_BUCKET) ?: DEFAULT_STORAGE_BUCKET
  }

  fun isConfigured(): Boolean {
    val url = getSupabaseUrl()
    val key = getAnonKey()
    return url.isNotBlank() &&
      url.startsWith("http") &&
      key.isNotBlank() &&
      !url.contains("your-project.supabase.co") &&
      !key.contains("placeholder_anon_key")
  }

  fun saveCredentials(url: String, anonKey: String, bucket: String = DEFAULT_STORAGE_BUCKET) {
    prefs.edit()
      .putString(KEY_URL, cleanUrl(url.trim()))
      .putString(KEY_ANON_KEY, anonKey.trim())
      .putString(KEY_STORAGE_BUCKET, bucket.trim().ifBlank { DEFAULT_STORAGE_BUCKET })
      .apply()
  }

  fun clearCredentials() {
    prefs.edit().clear().apply()
  }

  fun resolveImageUrl(rawImage: String): String {
    val trimmed = rawImage.trim()
    if (trimmed.isBlank()) return ""
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
      return trimmed
    }
    // If it's a relative path in Supabase Storage
    val baseUrl = getSupabaseUrl()
    if (baseUrl.isBlank()) return trimmed
    val bucket = getStorageBucket()
    val cleanPath = trimmed.trimStart('/')
    return "$baseUrl/storage/v1/object/public/$bucket/$cleanPath"
  }

  private fun cleanUrl(url: String): String {
    var cleaned = url.trim()
    if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
      cleaned = "https://$cleaned"
    }
    return cleaned.trimEnd('/')
  }
}
