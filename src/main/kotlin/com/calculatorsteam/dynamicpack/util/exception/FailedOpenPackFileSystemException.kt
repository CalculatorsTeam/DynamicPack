package com.calculatorsteam.dynamicpack.util.exception

/**
 * Thrown when fail to open pack file system.
 */
class FailedOpenPackFileSystemException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null,
    enableSuppression: Boolean = false,
    writableStackTrace: Boolean = true
) : RuntimeException(message, cause, enableSuppression, writableStackTrace)