package com.calculatorsteam.dynamicpack.util.enums

/**
 * Override state for pack configuration.
 */
enum class OverrideType {
    TRUE,
    FALSE,
    NOT_SET;

    fun next(): OverrideType = when (this) {
        TRUE -> FALSE
        FALSE -> NOT_SET
        NOT_SET -> TRUE
    }

    /**
     * Convert enum to boolean. Throws if value == NOT_SET.
     */
    fun asBoolean(): Boolean = when (this) {
        TRUE -> true
        FALSE -> false
        NOT_SET -> throw UnsupportedOperationException("asBoolean() doesn't support for NOT_SET")
    }

    /**
     * Convert enum to boolean with default if NOT_SET.
     */
    fun asBoolean(def: Boolean): Boolean = when (this) {
        TRUE -> true
        FALSE -> false
        NOT_SET -> def
    }

    companion object {
        @JvmStatic
        fun ofBoolean(b: Boolean): OverrideType = if (b) TRUE else FALSE
    }
}