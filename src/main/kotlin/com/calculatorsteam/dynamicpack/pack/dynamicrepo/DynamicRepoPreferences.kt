package com.calculatorsteam.dynamicpack.pack.dynamicrepo

import com.calculatorsteam.dynamicpack.InputValidator
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.util.enums.OverrideType
import com.calculatorsteam.dynamicpack.util.JsonUtil.getBoolean
import com.calculatorsteam.dynamicpack.util.JsonUtil.getJsonArray
import com.calculatorsteam.dynamicpack.util.JsonUtil.getString
import com.calculatorsteam.dynamicpack.util.JsonUtil.optBoolean
import com.calculatorsteam.dynamicpack.util.JsonUtil.optString
import com.calculatorsteam.dynamicpack.util.log.Out
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class DynamicRepoPreferences(
    private val pack: DynamicResourcePack,
    private val remote: DynamicRepoRemote
) {
    // if key exists, value used for override content. If content is required it ignored...
    private val contentOverrides = mutableMapOf<String, Boolean>()

    private val cachedRemoteJson: JsonObject = remote.cachedRemote
    private val cachedCurrentJson: JsonObject = remote.cachedCurrent

    init {
        recalculateContentOverrideFromJson()
    }

    /**
     * Update this.contentOverrides from cachedRemoteJson
     */
    private fun recalculateContentOverrideFromJson() {
        contentOverrides.clear()
        if (cachedRemoteJson.has("content_override")) {
            val j = cachedRemoteJson.getAsJsonObject("content_override")
            j.keySet().forEach { key ->
                contentOverrides[key] = j.getBoolean(key)
            }
            if (j.size() == 0) cachedCurrentJson.remove("content_override")
        }
    }

    fun notifyNewRemoteJson(repoJson: JsonObject) {
        val contents = repoJson.getJsonArray("contents") ?: JsonArray()
        val guis = repoJson.getJsonArray("gui")

        if (guis == null) {
            Out.debug("Repo not using 'gui' features.")
        }

        updateKnownContents(contents)
        updateKnownGuis(guis)
    }

    private fun updateKnownGuis(guis: JsonArray?) {
        if (guis != null) {
            validateGuis(guis)
            cachedCurrentJson.add("known_guis", guis)
        } else cachedCurrentJson.remove("known_guis")
    }

    private fun validateGuis(guis: JsonArray) {
        for (_gui in guis) {
            val gui = _gui.asJsonObject
            val type = gui.getString("type")
            val id = gui.getString("id")

            InputValidator.throwIsContentIdInvalid(id)
            InputValidator.throwIsContentIdInvalid(type)

            if (type.equals("enum", ignoreCase = true)) {
                val enums = gui.getAsJsonObject("enum")
                enums.keySet().forEach { key ->
                    InputValidator.throwIsContentIdInvalid(key)
                    val anEnum = enums.getAsJsonObject(key)

                    if (!InputValidator.isDynamicPackNameValid(anEnum.getString("name"))) {
                        throw RuntimeException("Name of enum element invalid :( enumKey=$key")
                    }

                    val contents = anEnum.getAsJsonObject("contents")
                    contents.keySet().forEach { contentId ->
                        // call get for validate is boolean
                        contents.get(contentId).asBoolean

                        val found = BaseContent.findById(getKnownContents(), contentId)
                        requireNotNull(found) { "Content from enum not found :(" }

                        if (found.required) {
                            throw RuntimeException("Override 'required':true content in enum not allowed!")
                        }
                    }
                }
            }
        }
    }

    private fun getContentJsonById(contents: JsonArray, findId: String): JsonObject? {
        Out.debug("getContentJsonById findId=$findId")
        return contents.firstOrNull {
            it.asJsonObject.getString("id").equals(findId, ignoreCase = true)
        }?.asJsonObject
    }

    private fun updateKnownContents(contents: JsonArray) {
        val knownContents = JsonObject()
        for (contentElem in contents) {
            val jsonObject = contentElem.asJsonObject
            val id = jsonObject.getString("id")
            val required = jsonObject.optBoolean("required", false)

            if (required) setContentOverride(id, OverrideType.NOT_SET)

            val excludeContent = parseContentList(jsonObject, "exclude_content")

            excludeContent.forEach { s ->
                val cont = getContentJsonById(contents, s)
                    ?: throw RuntimeException("exclude_content contains id not found content :(")
                if (cont.optBoolean("required", false)) {
                    throw RuntimeException("Exclude required content not allowed!")
                }
            }
            if (excludeContent.contains(id)) {
                throw RuntimeException("Self id in exclude list. Not allowed!")
            }

            val newJsonObject = jsonObject.deepCopy().apply { remove("id") }
            knownContents.add(id, newJsonObject)
        }
        cachedCurrentJson.add("known_contents", knownContents)
    }

    /** Is content active by overrides (only settings) */
    fun isContentActive(id: String, def: Boolean): Boolean {
        return contentOverrides[id] ?: def
    }

    fun getKnownContents(): Array<BaseContent> {
        return try {
            if (cachedCurrentJson.has("known_contents")) {
                val known = cachedCurrentJson.getAsJsonObject("known_contents")
                val list = mutableListOf<BaseContent>()
                known.keySet().forEach { contentId ->
                    val content = known.getAsJsonObject(contentId)
                    val required = content.optBoolean("required", false)
                    val defaultValue = content.optBoolean("default_active", true)
                    val hidden = content.optBoolean("hidden", false)
                    val name = content.optString("name", null)
                    val resultOverride =
                        if (required) OverrideType.TRUE else getCurrentOverrideStatus(contentId)
                    val exclude = parseContentList(content, "exclude_content")

                    list += BaseContent(remote, contentId, required, resultOverride, name, defaultValue, hidden, exclude)
                }
                list.toTypedArray()
            } else emptyArray()
        } catch (e: Exception) {
            pack.setLatestException(e)
            Out.error("Error while getKnownContents()", e)
            emptyArray()
        }
    }

    private fun parseContentList(content: JsonObject, key: String): Set<String> {
        val set = mutableSetOf<String>()
        if (content.has(key)) {
            val element = content.get(key)
            if (element.isJsonArray) {
                element.asJsonArray.forEach { jsonElement ->
                    val str = jsonElement.asString
                    if (!set.add(str)) throw RuntimeException("$key: duplicated!")
                }
            } else if (element.isJsonPrimitive) {
                set.add(element.asString)
            }
        }
        return set
    }

    private fun getCurrentOverrideStatus(contentId: String): OverrideType {
        return contentOverrides[contentId]?.let { OverrideType.ofBoolean(it) } ?: OverrideType.NOT_SET
    }

    fun setContentOverride(content: BaseContent, overrideType: OverrideType) {
        val effective = if (content.required) OverrideType.NOT_SET else overrideType
        setContentOverride(content.id, effective)
    }

    fun setContentOverride(id: String, overrideType: OverrideType) {
        Out.debug("setContentOverride: $id: $overrideType")
        val override: JsonObject? = if (cachedRemoteJson.has("content_override")) {
            cachedRemoteJson.getAsJsonObject("content_override")
        } else if (overrideType != OverrideType.NOT_SET) {
            JsonObject()
        } else null

        if (override != null) {
            if (overrideType == OverrideType.NOT_SET) {
                override.remove(id)
            } else {
                override.addProperty(id, overrideType.asBoolean())
            }
            if (override.keySet().isEmpty()) {
                cachedRemoteJson.remove("content_override")
            } else if (!cachedRemoteJson.has("content_override")) {
                cachedRemoteJson.add("content_override", override)
            }
        }
        recalculateContentOverrideFromJson()
    }

    fun getKnownEnums(): Array<BaseEnum> {
        return try {
            if (cachedCurrentJson.has("known_guis")) {
                val known = cachedCurrentJson.getAsJsonArray("known_guis")
                val enums = mutableListOf<BaseEnum>()
                known.forEach { elem ->
                    val jsonEnum = elem.asJsonObject
                    // type must be enum
                    if (!jsonEnum.getString("type").equals("enum", ignoreCase = true)) return@forEach
                    enums += BaseEnum.ofJson(jsonEnum)
                }
                enums.toTypedArray()
            } else emptyArray()
        } catch (e: Exception) {
            pack.setLatestException(e)
            Out.error("Error while getKnownEnums()", e)
            emptyArray()
        }
    }
}