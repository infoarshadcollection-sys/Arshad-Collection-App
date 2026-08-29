package com.example.data.local

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class Converters {
  @TypeConverter
  fun fromStringList(list: List<String>?): String {
    if (list == null) return "[]"
    val jsonArray = JSONArray()
    list.forEach { jsonArray.put(it) }
    return jsonArray.toString()
  }

  @TypeConverter
  fun toStringList(json: String?): List<String> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
      val jsonArray = JSONArray(json)
      val list = mutableListOf<String>()
      for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }
}
