package com.calculatorsteam.dynamicpack.util

import com.calculatorsteam.dynamicpack.Constants
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path

/**
 * <h2>Preserving the psyche during migration from org.json to gson</h2>
 * @author AdamCalculator
 */
object JsonUtil {
    // --- GET ---
    @JvmStatic
    fun JsonObject.optInt(key: String, def: Int = 0): Int =
        if (has(key)) getAsJsonPrimitive(key).asInt else def

    @JvmStatic
    fun JsonObject.getInt(key: String): Int =
        getAsJsonPrimitive(key).asInt

    @JvmStatic
    fun JsonObject.optLong(key: String, def: Long = 0L): Long =
        if (has(key)) getAsJsonPrimitive(key).asLong else def

    @JvmStatic
    fun JsonObject.getLong(key: String): Long =
        getAsJsonPrimitive(key).asLong

    @JvmStatic
    fun JsonObject.getString(key: String): String =
        getAsJsonPrimitive(key).asString

    @JvmStatic
    fun JsonObject.optString(key: String, def: String? = null): String? =
        if (has(key)) getAsJsonPrimitive(key).asString else def

    @JvmStatic
    fun JsonObject.getJsonArray(key: String): JsonArray? =
        if (has(key)) getAsJsonArray(key) else null

    @JvmStatic
    fun JsonObject.getBoolean(key: String): Boolean =
        getAsJsonPrimitive(key).asBoolean

    @JvmStatic
    fun JsonObject.optBoolean(key: String, def: Boolean = false): Boolean =
        if (has(key)) get(key).asBoolean else def

    // --- CREATE ---
    @JvmStatic
    fun fromString(s: String): JsonObject =
        Constants.GSON.fromJson(s, JsonObject::class.java)

    @JvmStatic
    @Throws(IOException::class)
    fun readJson(inputStream: InputStream): JsonObject =
        fromString(PathUtil.readString(inputStream))

    @JvmStatic
    @Throws(IOException::class)
    fun readJson(path: Path): JsonObject =
        fromString(PathUtil.readString(path))

    @JvmStatic
    fun arrayFromString(s: String): JsonArray =
        Constants.GSON.fromJson(s, JsonArray::class.java)

    @JvmStatic
    fun toString(json: JsonObject): String =
        Constants.GSON.toJson(json)
}