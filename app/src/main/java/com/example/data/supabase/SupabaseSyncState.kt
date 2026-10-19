package com.example.data.supabase

sealed interface SupabaseSyncStatus {
  object Idle : SupabaseSyncStatus
  object Connecting : SupabaseSyncStatus
  data class Syncing(val message: String = "Fetching products from Supabase...") : SupabaseSyncStatus
  data class Success(val productCount: Int, val timestamp: Long = System.currentTimeMillis()) : SupabaseSyncStatus
  data class Error(val message: String, val timestamp: Long = System.currentTimeMillis()) : SupabaseSyncStatus
}

data class SupabaseSchemaInfo(
  val detectedProductsTable: String = "products",
  val detectedCategoriesTable: String = "categories",
  val detectedColumns: List<String> = emptyList(),
  val hasRealtimeEnabled: Boolean = true,
  val isSchemaInspected: Boolean = false
)

sealed interface RealtimeConnectionState {
  object Disconnected : RealtimeConnectionState
  object Connecting : RealtimeConnectionState
  data class Connected(val channel: String = "products") : RealtimeConnectionState
  data class Error(val reason: String) : RealtimeConnectionState
}
