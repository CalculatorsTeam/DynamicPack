package com.calculatorsteam.dynamicpack.pack.dynamicrepo

import com.calculatorsteam.dynamicpack.util.enums.OverrideType

/**
 * Base content description in DynamicRepoRemote.
 *
 * BREAKING CHANGES:
 * since 1.0.31 ignoring [required] key.
 */
class BaseContent(
    val parentRemote: DynamicRepoRemote,
    val id: String,
    val required: Boolean,
    var overrideType: OverrideType,
    val name: String?,
    private val defaultStatus: Boolean,
    val hidden: Boolean,
    val exclude: Set<String>
) {

    /**
     * Switch override value to next state.
     */
    fun nextOverride(baseContents: Array<BaseContent>) {
        setOverrideType(overrideType.next(), baseContents)
    }

    /**
     * Set override value and enforce exclusions.
     */
    fun setOverrideType(override: OverrideType, baseContents: Array<BaseContent>) {
        overrideType = override
        val status = state

        if (status) {
            exclude.forEach { excludeId ->
                val byId = findById(baseContents, excludeId)
                    ?: throw NullPointerException("Content with id=$excludeId not found")

                if (byId.required) {
                    throw RuntimeException("Exclude a required not allowed!")
                }
                if (byId.state) {
                    byId.setOverrideType(OverrideType.FALSE, baseContents)
                }
            }
        }
    }

    /** Current effective state */
    private val state: Boolean
        get() = overrideType.asBoolean(required || defaultStatus)

    val defaultState: Boolean
        get() = defaultStatus

    companion object {
        @JvmStatic
        fun findById(contents: Array<BaseContent>, findId: String): BaseContent? =
            contents.firstOrNull { it.id.equals(findId, ignoreCase = true) }
    }
}