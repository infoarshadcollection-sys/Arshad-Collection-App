package com.example.data.supabase

import android.net.Uri
import android.util.Log
import com.example.data.model.Product
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

interface SupabaseRealtimeListener {
  fun onProductUpserted(product: Product)
  fun onProductDeleted(supabaseId: String)
  fun onCategoryChanged()
  fun onConnectionChanged(isConnected: Boolean)
}

class SupabaseRealtimeManager(
  private val config: SupabaseConfig,
  private val client: SupabaseClient,
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

  companion object {
    private const val TAG = "SupabaseRealtime"
    private const val HEARTBEAT_INTERVAL_MS = 25000L
  }

  private val _connectionState = MutableStateFlow<RealtimeConnectionState>(RealtimeConnectionState.Disconnected)
  val connectionState: StateFlow<RealtimeConnectionState> = _connectionState.asStateFlow()

  private var webSocket: WebSocket? = null
  private var heartbeatJob: Job? = null
  private var reconnectJob: Job? = null
  private val refCounter = AtomicInteger(1)
  private var listener: SupabaseRealtimeListener? = null
  private var isIntentionallyClosed = false

  private val okHttpClient = OkHttpClient.Builder()
    .readTimeout(0, TimeUnit.MILLISECONDS) // Keep-alive for websockets
    .connectTimeout(15, TimeUnit.SECONDS)
    .pingInterval(20, TimeUnit.SECONDS)
    .build()

  fun setListener(listener: SupabaseRealtimeListener?) {
    this.listener = listener
  }

  fun connect() {
    if (!config.isConfigured()) {
      Log.d(TAG, "Cannot connect to Realtime: Supabase credentials not configured.")
      _connectionState.value = RealtimeConnectionState.Disconnected
      return
    }

    if (_connectionState.value is RealtimeConnectionState.Connected ||
      _connectionState.value is RealtimeConnectionState.Connecting) {
      return
    }

    isIntentionallyClosed = false
    _connectionState.value = RealtimeConnectionState.Connecting

    val rawUrl = config.getSupabaseUrl()
    val anonKey = config.getAnonKey()

    try {
      val parsedUri = Uri.parse(rawUrl)
      val host = parsedUri.host ?: return
      val wsUrl = "wss://$host/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"

      val request = Request.Builder()
        .url(wsUrl)
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .build()

      webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
          Log.d(TAG, "Supabase Realtime WebSocket connected.")
          _connectionState.value = RealtimeConnectionState.Connected("products")
          listener?.onConnectionChanged(true)
          startHeartbeat()
          subscribeToChannels(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
          handleIncomingMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
          Log.d(TAG, "WebSocket closing: $code / $reason")
          webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
          Log.d(TAG, "WebSocket closed: $code / $reason")
          _connectionState.value = RealtimeConnectionState.Disconnected
          listener?.onConnectionChanged(false)
          stopHeartbeat()
          scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
          Log.w(TAG, "WebSocket failure: ${t.message}")
          _connectionState.value = RealtimeConnectionState.Error(t.message ?: "Connection failure")
          listener?.onConnectionChanged(false)
          stopHeartbeat()
          scheduleReconnect()
        }
      })
    } catch (e: Exception) {
      Log.e(TAG, "Error initiating realtime connection", e)
      _connectionState.value = RealtimeConnectionState.Error(e.message ?: "Failed to connect")
      scheduleReconnect()
    }
  }

  fun disconnect() {
    isIntentionallyClosed = true
    stopHeartbeat()
    reconnectJob?.cancel()
    webSocket?.close(1000, "App paused or disconnected")
    webSocket = null
    _connectionState.value = RealtimeConnectionState.Disconnected
  }

  private fun subscribeToChannels(ws: WebSocket) {
    val productsTable = client.schemaInfo.detectedProductsTable.ifBlank { "products" }
    val categoriesTable = client.schemaInfo.detectedCategoriesTable.ifBlank { "categories" }

    // Join products postgres_changes
    val productsJoinMsg = JSONObject().apply {
      put("topic", "realtime:public:$productsTable")
      put("event", "phx_join")
      put("ref", refCounter.getAndIncrement().toString())
      put("payload", JSONObject().apply {
        put("config", JSONObject().apply {
          put("broadcast", JSONObject().apply { put("self", false) })
          put("presence", JSONObject().apply { put("key", "") })
          put("postgres_changes", JSONArray().apply {
            put(JSONObject().apply {
              put("event", "*")
              put("schema", "public")
              put("table", productsTable)
            })
          })
        })
      })
    }
    ws.send(productsJoinMsg.toString())

    // Join categories postgres_changes
    val categoriesJoinMsg = JSONObject().apply {
      put("topic", "realtime:public:$categoriesTable")
      put("event", "phx_join")
      put("ref", refCounter.getAndIncrement().toString())
      put("payload", JSONObject().apply {
        put("config", JSONObject().apply {
          put("broadcast", JSONObject().apply { put("self", false) })
          put("presence", JSONObject().apply { put("key", "") })
          put("postgres_changes", JSONArray().apply {
            put(JSONObject().apply {
              put("event", "*")
              put("schema", "public")
              put("table", categoriesTable)
            })
          })
        })
      })
    }
    ws.send(categoriesJoinMsg.toString())
    Log.d(TAG, "Sent join subscriptions for tables '$productsTable' & '$categoriesTable'")
  }

  private fun startHeartbeat() {
    heartbeatJob?.cancel()
    heartbeatJob = scope.launch {
      while (isActive) {
        delay(HEARTBEAT_INTERVAL_MS)
        try {
          val heartbeat = JSONObject().apply {
            put("topic", "phoenix")
            put("event", "heartbeat")
            put("payload", JSONObject())
            put("ref", refCounter.getAndIncrement().toString())
          }
          webSocket?.send(heartbeat.toString())
        } catch (e: Exception) {
          Log.w(TAG, "Heartbeat failed: ${e.message}")
        }
      }
    }
  }

  private fun stopHeartbeat() {
    heartbeatJob?.cancel()
    heartbeatJob = null
  }

  private fun scheduleReconnect() {
    if (isIntentionallyClosed) return
    reconnectJob?.cancel()
    reconnectJob = scope.launch {
      delay(5000L) // Wait 5 seconds before reconnecting
      if (!isIntentionallyClosed && config.isConfigured()) {
        Log.d(TAG, "Attempting Supabase Realtime reconnection...")
        connect()
      }
    }
  }

  private fun handleIncomingMessage(text: String) {
    try {
      val obj = JSONObject(text)
      val event = obj.optString("event")
      val topic = obj.optString("topic")

      if (event == "postgres_changes") {
        val payload = obj.optJSONObject("payload") ?: return
        val data = payload.optJSONObject("data") ?: return
        val eventType = data.optString("type") // INSERT, UPDATE, DELETE
        val record = data.optJSONObject("record")
        val oldRecord = data.optJSONObject("old_record")

        Log.d(TAG, "Realtime Postgres Event received: $eventType on $topic")

        if (topic.contains("categories")) {
          listener?.onCategoryChanged()
          return
        }

        when (eventType) {
          "INSERT", "UPDATE" -> {
            if (record != null) {
              val product = client.parseProductFromJson(record)
              if (product != null) {
                listener?.onProductUpserted(product)
              }
            }
          }
          "DELETE" -> {
            val idToDelete = when {
              oldRecord?.has("id") == true -> oldRecord.opt("id")?.toString()
              oldRecord?.has("product_id") == true -> oldRecord.opt("product_id")?.toString()
              record?.has("id") == true -> record.opt("id")?.toString()
              else -> null
            }
            if (!idToDelete.isNullOrBlank()) {
              listener?.onProductDeleted(idToDelete)
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error handling incoming realtime message: ${e.message}")
    }
  }
}
