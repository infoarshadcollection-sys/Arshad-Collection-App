package com.example.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.supabase.SupabaseClient
import com.example.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageService {
  private const val TAG = "ImageStorageService"
  const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10MB limit

  val ALLOWED_MIME_TYPES = setOf(
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp"
  )

  /**
   * Validates and persistently stores an image.
   * If Supabase is configured, uploads directly to Supabase Storage and returns the public URL.
   * Also ensures a permanent local backup exists so the image is never lost and works offline.
   */
  suspend fun storeImage(
    context: Context,
    uri: Uri,
    folder: String, // "products" or "categories"
    supabaseClient: SupabaseClient?,
    supabaseConfig: SupabaseConfig?
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val contentResolver = context.contentResolver
      val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

      // 1. Validate File Type
      if (!isMimeTypeAllowed(mimeType)) {
        return@withContext Result.failure(
          IllegalArgumentException("Invalid file type: $mimeType. Only JPEG, PNG, and WebP images are allowed.")
        )
      }

      // 2. Validate File Size & Read Bytes
      val inputStream = contentResolver.openInputStream(uri)
        ?: return@withContext Result.failure(IllegalArgumentException("Cannot read image from selected URI."))

      val bytes = inputStream.use { it.readBytes() }
      if (bytes.isEmpty()) {
        return@withContext Result.failure(IllegalArgumentException("Selected image file is empty."))
      }
      if (bytes.size > MAX_FILE_SIZE_BYTES) {
        val sizeMb = bytes.size / (1024.0 * 1024.0)
        return@withContext Result.failure(
          IllegalArgumentException("File is too large (${String.format("%.1f", sizeMb)}MB). Maximum allowed size is 10MB.")
        )
      }

      // 3. Generate unique file name
      val extension = when {
        mimeType.contains("png", ignoreCase = true) -> "png"
        mimeType.contains("webp", ignoreCase = true) -> "webp"
        else -> "jpg"
      }
      val fileName = "${folder}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension"

      // 4. Save permanently in internal storage (never deleted by system cache cleaning)
      val uploadDir = File(context.filesDir, "arshad_uploads/$folder")
      if (!uploadDir.exists()) {
        uploadDir.mkdirs()
      }
      val localFile = File(uploadDir, fileName)
      FileOutputStream(localFile).use { out ->
        out.write(bytes)
        out.flush()
      }
      val localFileUri = "file://${localFile.absolutePath}"
      Log.d(TAG, "Image saved locally: $localFileUri (${bytes.size} bytes)")

      // 5. If Supabase is configured, attempt upload to cloud storage bucket
      if (supabaseConfig != null && supabaseConfig.isConfigured() && supabaseClient != null) {
        val bucket = supabaseConfig.getStorageBucket().ifBlank { "products" }
        val storagePath = "$folder/$fileName"
        Log.d(TAG, "Uploading image to Supabase Storage: bucket=$bucket, path=$storagePath")

        val uploadResult = supabaseClient.uploadStorageFile(
          bucket = bucket,
          path = storagePath,
          bytes = bytes,
          mimeType = mimeType
        )

        if (uploadResult.isSuccess) {
          val publicUrl = uploadResult.getOrThrow()
          Log.i(TAG, "Supabase storage upload succeeded: $publicUrl")
          return@withContext Result.success(publicUrl)
        } else {
          Log.w(TAG, "Supabase storage upload failed, falling back to local persistent copy: ${uploadResult.exceptionOrNull()?.message}")
        }
      }

      // If Supabase was not configured or upload encountered an issue, use permanent local copy
      Result.success(localFileUri)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to store image", e)
      Result.failure(e)
    }
  }

  private fun isMimeTypeAllowed(mimeType: String): Boolean {
    val clean = mimeType.lowercase().trim()
    return ALLOWED_MIME_TYPES.any { clean.startsWith(it) } || clean.startsWith("image/")
  }
}
