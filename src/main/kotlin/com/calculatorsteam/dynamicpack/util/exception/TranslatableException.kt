package com.calculatorsteam.dynamicpack.util.exception

import net.minecraft.network.chat.Component

/**
 * Exception shows in GUI as translatable
 */
class TranslatableException : RuntimeException {

    val key: String
    val args: Array<out Any>

    constructor(message: String?, key: String, vararg args: Any) : super(message) {
        this.key = key
        this.args = args
    }

    constructor(message: String?, cause: Throwable?, key: String, vararg args: Any) : super(message, cause) {
        this.key = key
        this.args = args
    }

    constructor(cause: Throwable?, key: String, vararg args: Any) : super(cause) {
        this.key = key
        this.args = args
    }

    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean,
        key: String,
        vararg args: Any
    ) : super(message, cause, enableSuppression, writableStackTrace) {
        this.key = key
        this.args = args
    }

    companion object {
        fun findComponentOnException(e: Throwable?): Component? {
            if (e == null) return null
            return if (e is TranslatableException) {
                Component.translatable(e.key, *e.args)
            } else if (e.cause != null) {
                findComponentOnException(e.cause)
            } else null
        }

        @JvmStatic
        fun getComponentFromException(e: Throwable): Component {
            return findComponentOnException(e) ?: Component.literal(e.message ?: "Unknown error")
        }
    }
}