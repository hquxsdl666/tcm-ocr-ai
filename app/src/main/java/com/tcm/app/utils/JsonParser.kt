package com.tcm.app.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.tcm.app.data.remote.model.OcrResult

object JsonParser {
    private val gson = Gson()

    fun parseOcrResult(jsonString: String): OcrResult? {
        return try {
            gson.fromJson(jsonString, OcrResult::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    fun toJson(obj: Any): String {
        return gson.toJson(obj)
    }

    inline fun <reified T> fromJson(jsonString: String): T? {
        return try {
            gson.fromJson(jsonString, object : TypeToken<T>() {}.type)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    fun extractJsonFromMarkdown(text: String): String {
        // Extract JSON from markdown code blocks if present
        val jsonPattern = """```(?:json)?\s*([\s\S]*?)```""".toRegex()
        val match = jsonPattern.find(text)
        return match?.groupValues?.get(1)?.trim() ?: text.trim()
    }
}
