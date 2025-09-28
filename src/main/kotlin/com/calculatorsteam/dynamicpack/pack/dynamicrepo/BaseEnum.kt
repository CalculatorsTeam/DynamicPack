package com.calculatorsteam.dynamicpack.pack.dynamicrepo

import com.calculatorsteam.dynamicpack.util.enums.OverrideType
import com.calculatorsteam.dynamicpack.util.JsonUtil.getBoolean
import com.calculatorsteam.dynamicpack.util.JsonUtil.getString
import com.calculatorsteam.dynamicpack.util.JsonUtil.optString
import com.google.gson.JsonObject

/**
 * Representation of an "enum" in dynamic repo remote.
 */
class BaseEnum private constructor(
    val id: String,
    val name: String,
    private val elements: LinkedHashMap<String, Element>
) {

    companion object {
        fun ofJson(json: JsonObject): BaseEnum {
            val id = json.getString("id")
            val name = json.optString("name", "Unknown") ?: "Unknown"

            val enumJson = json.getAsJsonObject("enum")
            val elements = LinkedHashMap<String, Element>()

            for (key in enumJson.keySet()) {
                val element = Element.ofJson(enumJson.getAsJsonObject(key))
                elements[key] = element
            }

            return BaseEnum(id, name, elements)
        }
    }

    /** Returns the display name of the current state */
    fun getCurrentState(contents: Array<BaseContent>): String =
        getCurrentElement(contents)?.name ?: "Unknown"

    /** Returns the current matching element if any */
    fun getCurrentElement(contents: Array<BaseContent>): Element? {
        return elements.values.firstOrNull { element ->
            element.contents.all { (contentId, requiredBool) ->
                val baseContent = BaseContent.findById(contents, contentId)
                baseContent?.overrideType?.asBoolean(baseContent.defaultState) == requiredBool
            }
        }
    }

    /**
     * Cycle to next element, apply its overrides.
     */
    @Throws(Exception::class)
    fun applyNext(contents: Array<BaseContent>) {
        val current = getCurrentElement(contents)
        val values = elements.values.toList()

        val next = if (current == null) {
            values.firstOrNull()
        } else {
            val index = values.indexOf(current)
            if (index == -1) values.firstOrNull()
            else values[(index + 1) % values.size]
        }

        next?.apply(contents)
    }

    data class Element(
        val name: String,
        val contents: Map<String, Boolean>
    ) {
        companion object {
            fun ofJson(json: JsonObject): Element {
                val name = json.getString("name")
                val contents = mutableMapOf<String, Boolean>()

                val jsonContents = json.getAsJsonObject("contents")
                for (contentId in jsonContents.keySet()) {
                    val bool = jsonContents.getBoolean(contentId)
                    contents[contentId] = bool
                }
                return Element(name, contents)
            }
        }

        @Throws(Exception::class)
        fun apply(contentsArray: Array<BaseContent>) {
            contents.forEach { (contentId, bool) ->
                val base = BaseContent.findById(contentsArray, contentId)
                    ?: throw RuntimeException("Content with id=$contentId not found")
                base.setOverrideType(OverrideType.ofBoolean(bool), contentsArray)
            }
        }
    }
}